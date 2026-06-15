package com.github.objoraddd.blackjack.application.table;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.github.objoraddd.blackjack.domain.gateway.UserGateway;
import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.events.ChargeEvent;
import com.github.objoraddd.blackjack.domain.table.events.DomainEvent;
import com.github.objoraddd.blackjack.domain.table.events.PayoutEvent;
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
    private final OutboxPublisher outboxPublisher;
    private final UserGateway userGateway;
    private final TransactionalOperator transactionalOperator;

    public TableService(TableRepository tableRepository, OutboxPublisher outboxPublisher,
            UserGateway userGateway, TransactionalOperator transactionalOperator) {
        this.tableRepository = tableRepository;
        this.userGateway = userGateway;
        this.outboxPublisher = outboxPublisher;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Table> createTable(String userId, String username) {
        return userGateway.getUserBalance(UserId.of(userId))
                .flatMap(balance -> {
                    if (balance == null || balance.isZero()) {
                        return Mono.error(ServiceException.zeroBalanceException());
                    }

                    Player player = new Player(UserId.of(userId), Username.of(username), balance);
                    Table newTable = new Table(TableId.of(UUID.randomUUID().toString()), player);

                    return tableRepository.create(newTable);
                }).onErrorMap(org.springframework.dao.DataIntegrityViolationException.class,
                        ex -> ServiceException.playerAlreadyInGameException());
    }

    public Mono<Table> placeBet(String tableId, Long betAmount) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    Money domainBet = Money.of(betAmount);
                    table.placeBet(domainBet);

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

    public Mono<Void> closeTable(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    UserId userId = table.getPlayer().getUserId();
                    long betAmount = table.getPlayer().getBet().getAmount();

                    Mono<Void> deletion = tableRepository.deleteById(table.getId());

                    if (table.getStatus() != GameStatus.FINISHED || table.getResult() == null) {
                        DomainEvent chargeEvent = new ChargeEvent(TableId.of(tableId), userId, Money.of(betAmount),
                                java.time.Instant.now());
                        return deletion.then(outboxPublisher.publish(chargeEvent)).then();
                    }

                    switch (table.getResult()) {
                        case PLAYER_WON -> {
                            long netProfit = table.calculatePayout().getAmount() - betAmount;
                            DomainEvent payoutEvent = new PayoutEvent(TableId.of(tableId), userId, Money.of(netProfit),
                                    java.time.Instant.now());
                            return deletion.then(outboxPublisher.publish(payoutEvent)).then();
                        }
                        case DEALER_WON -> {
                            DomainEvent chargeEvent = new ChargeEvent(TableId.of(tableId), userId, Money.of(betAmount),
                                    java.time.Instant.now());
                            return deletion.then(outboxPublisher.publish(chargeEvent)).then();
                        }
                        default -> {
                            return deletion;
                        }
                    }
                })
                .as(transactionalOperator::transactional);
    }

    private void handlePossibleGameOver(Table table) {
        if (table.getStatus() == GameStatus.FINISHED) {
            Money payout = table.calculatePayout();

            table.getPlayer().topUpBalance(payout);
        }
    }
}