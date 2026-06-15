package com.github.objoraddd.blackjack.application.table;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.github.objoraddd.blackjack.domain.gateway.UserGateway;
import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.events.ChargeEvent;
import com.github.objoraddd.blackjack.domain.table.events.DomainEvent;
import com.github.objoraddd.blackjack.domain.table.events.RefundEvent;
import com.github.objoraddd.blackjack.domain.table.events.WinEvent;
import com.github.objoraddd.blackjack.domain.table.valueobjects.DeckCount;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameResult;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameStatus;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Username;
import com.github.objoraddd.blackjack.application.exceptions.ServiceException;
import com.github.objoraddd.blackjack.application.outbox.OutboxPublisher;

import reactor.core.publisher.Mono;

@Service
public final class TableService {

    private final TableRepository tableRepository;
    private final UserGateway userGateway;
    private final TransactionalOperator transactionalOperator;
    private final OutboxPublisher outboxPublisher;

    public TableService(TableRepository tableRepository, UserGateway userGateway,
            TransactionalOperator transactionalOperator, OutboxPublisher outboxPublisher) {
        this.tableRepository = tableRepository;
        this.userGateway = userGateway;
        this.transactionalOperator = transactionalOperator;
        this.outboxPublisher = outboxPublisher;
    }

    public Mono<Table> createTable(String userIdString, String username, int deckCount) {
        UserId userId = UserId.of(userIdString);

        return userGateway.holdBalance(userId)
                .flatMap(balance -> {
                    Player player = Player.createNewPlayer(userId, Username.of(username), balance);
                    Table newTable = Table.createNewTable(TableId.of(UUID.randomUUID().toString()), player,
                            DeckCount.of(deckCount));

                    return tableRepository.create(newTable)
                            .flatMap(createdTable -> userGateway.approveHold(userId).thenReturn(createdTable));
                })
                .onErrorMap(org.springframework.dao.DataIntegrityViolationException.class,
                        ex -> ServiceException.playerAlreadyInGameException())
                .onErrorResume(ex -> userGateway.rejectHold(userId).then(Mono.error(ex)));
    }

    public Mono<Table> placeBet(String tableId, Long betAmount) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    table.placeBet(Money.of(betAmount));
                    return tableRepository.update(table);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> startNewGame(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    table.start();
                    handlePossibleGameOver(table);
                    return tableRepository.update(table);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> hit(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    table.playerHit();
                    handlePossibleGameOver(table);
                    return tableRepository.update(table);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> stand(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    table.playerStand();
                    handlePossibleGameOver(table);
                    return tableRepository.update(table);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> prepareNextRound(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    try {
                        table.nextRound();
                        return tableRepository.update(table);
                    } catch (com.github.objoraddd.blackjack.domain.exceptions.DomainException ex) {
                        return Mono.error(ServiceException.activeSessionException());
                    }
                }).as(transactionalOperator::transactional);
    }

    public Mono<DomainEvent> closeTable(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    UserId userId = table.getPlayer().getUserId();
                    long currentBalance = table.getPlayer().getBalance().getAmount();
                    long initialHold = table.getPlayer().getInitialBalance().getAmount();
                    long diff = currentBalance - initialHold;
                    TableId tId = TableId.of(tableId);
                    Instant now = Instant.now();

                    return tableRepository.deleteById(table.getId())
                            .then(Mono.defer(() -> {
                                if (table.getStatus() != GameStatus.FINISHED
                                        || table.getResult() == GameResult.DEALER_WON || diff < 0) {
                                    long loss = Math.abs(diff);
                                    DomainEvent chargeEvent = new ChargeEvent(tId, userId, Money.of(loss), now);
                                    return outboxPublisher.publish(chargeEvent);
                                } else if (diff > 0) {
                                    DomainEvent winEvent = new WinEvent(tId, userId, Money.of(diff), now);
                                    return outboxPublisher.publish(winEvent);
                                } else {
                                    DomainEvent refundEvent = new RefundEvent(tId, userId, Money.of(initialHold),
                                            now);
                                    return outboxPublisher.publish(refundEvent);
                                }
                            }));
                }).as(transactionalOperator::transactional);
    }

    private void handlePossibleGameOver(Table table) {
        if (table.getStatus() == GameStatus.FINISHED) {
            Money payout = table.calculatePayout();
            table.getPlayer().topUpBalance(payout);
        }
    }
}