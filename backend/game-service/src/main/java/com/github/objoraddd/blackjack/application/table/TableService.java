package com.github.objoraddd.blackjack.application.table;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.github.objoraddd.blackjack.domain.gateway.UserGateway;
import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameResult;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameStatus;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Username;
import com.github.objoraddd.blackjack.application.exceptions.ServiceException;

import reactor.core.publisher.Mono;

@Service
public final class TableService {

    private final TableRepository tableRepository;
    private final UserGateway userGateway;
    private final TransactionalOperator transactionalOperator;

    private static final Money SESSION_AMOUNT = Money.of(10000L);

    public TableService(TableRepository tableRepository, UserGateway userGateway,
            TransactionalOperator transactionalOperator) {
        this.tableRepository = tableRepository;
        this.userGateway = userGateway;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Table> createTable(String userIdString, String username) {
        UserId userId = UserId.of(userIdString);

        return userGateway.holdBalance(userId)
                .then(Mono.defer(() -> {
                    Player player = new Player(userId, Username.of(username), SESSION_AMOUNT);
                    Table newTable = new Table(TableId.of(UUID.randomUUID().toString()), player);

                    return tableRepository.create(newTable)
                            .flatMap(createdTable -> userGateway.approveHold(userId).thenReturn(createdTable))
                            .onErrorResume(ex -> userGateway.rejectHold(userId).then(Mono.error(ex)));
                }))
                .onErrorMap(org.springframework.dao.DataIntegrityViolationException.class,
                        ex -> ServiceException.playerAlreadyInGameException());
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
                    if (table.getStatus() != GameStatus.FINISHED) {
                        return Mono.error(ServiceException.activeSessionException());
                    }

                    Table resetTable = new Table(
                            table.getId(),
                            table.getPlayer(),
                            com.github.objoraddd.blackjack.domain.table.entities.Deck.createStandardDeck(),
                            com.github.objoraddd.blackjack.domain.table.valueobjects.Hand.emptyHand(),
                            GameStatus.WAGER_PLACEMENT,
                            null);
                    resetTable.getPlayer().clearHand();

                    return tableRepository.update(resetTable);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Void> closeTable(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    UserId userId = table.getPlayer().getUserId();
                    long currentBalance = table.getPlayer().getBalance().getAmount();
                    long initialHold = SESSION_AMOUNT.getAmount();
                    long diff = currentBalance - initialHold;

                    return tableRepository.deleteById(table.getId())
                            .then(Mono.defer(() -> {
                                if (table.getStatus() != GameStatus.FINISHED
                                        || table.getResult() == GameResult.DEALER_WON || diff < 0) {
                                    return userGateway.approveHold(userId);
                                } else if (diff > 0) {
                                    return userGateway.approveHold(userId)
                                            .then(userGateway.payout(userId, Money.of(diff)));
                                } else {
                                    return userGateway.approveHold(userId);
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