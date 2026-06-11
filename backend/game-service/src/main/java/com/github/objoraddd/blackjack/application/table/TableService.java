package com.github.objoraddd.blackjack.application.table;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.events.BetEvent;
import com.github.objoraddd.blackjack.domain.table.events.DomainEvent;
import com.github.objoraddd.blackjack.domain.table.events.WinEvent;
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
    private final TransactionalOperator transactionalOperator;

    public TableService(TableRepository tableRepository, OutboxPublisher outboxPublisher,
            TransactionalOperator transactionalOperator) {
        this.tableRepository = tableRepository;
        this.outboxPublisher = outboxPublisher;
        this.transactionalOperator = transactionalOperator;
    }

    public Mono<Table> createTable(String userId, String username) {
        return Mono.fromCallable(() -> {

            Player player = new Player(UserId.of(userId), Username.of(username));

            return new Table(TableId.of(UUID.randomUUID().toString()), player);
        }).flatMap(tableRepository::save);
    }

    public Mono<Table> placeBet(String tableId, Long betAmount) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {
                    UserId userId = table.getPlayer().getUserId();
                    Money domainBet = Money.of(betAmount);

                    table.placeBet(domainBet);

                    DomainEvent betEvent = new BetEvent(
                            table.getId(),
                            userId,
                            domainBet,
                            java.time.Instant.now());

                    return tableRepository.save(table)
                            .then(outboxPublisher.publish(
                                    betEvent))
                            .thenReturn(table);
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> startNewGame(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {

                    table.start();

                    return tableRepository.save(table)
                            .flatMap(savedTable -> {
                                if (savedTable.getStatus() == GameStatus.FINISHED) {
                                    Money payoutAmount = savedTable.calculatePayout();
                                    UserId userId = savedTable.getPlayer().getUserId();

                                    WinEvent winEvent = new WinEvent(
                                            savedTable.getId(),
                                            userId,
                                            payoutAmount,
                                            java.time.Instant.now());

                                    return outboxPublisher.publish(winEvent)
                                            .thenReturn(savedTable);
                                }
                                return Mono.just(savedTable);
                            });
                }).as(transactionalOperator::transactional);
    }

    public Mono<Table> hit(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {

                    table.playerHit();

                    return tableRepository.save(table);
                });
    }

    public Mono<Table> stand(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {

                    table.playerStand();

                    return tableRepository.save(table)
                            .flatMap(savedTable -> {
                                Money payoutAmount = savedTable.calculatePayout();

                                if (!payoutAmount.isZero()) {
                                    UserId userId = savedTable.getPlayer().getUserId();

                                    WinEvent winEvent = new WinEvent(
                                            savedTable.getId(),
                                            userId,
                                            payoutAmount,
                                            java.time.Instant.now());

                                    return outboxPublisher.publish(winEvent)
                                            .thenReturn(savedTable);
                                }

                                return Mono.just(savedTable);
                            });
                }).as(transactionalOperator::transactional);
    }

    public Mono<Void> closeTable(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> tableRepository.deleteById(table.getId()));
    }
}
