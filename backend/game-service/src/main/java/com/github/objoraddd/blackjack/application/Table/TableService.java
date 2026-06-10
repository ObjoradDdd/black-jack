package com.github.objoraddd.blackjack.application.Table;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.objoraddd.blackjack.domain.Table.Table;
import com.github.objoraddd.blackjack.domain.Table.entities.Player;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.GameStatus;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Username;
import com.github.objoraddd.blackjack.domain.gateway.UserGateway;
import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.application.exceptions.ServiceException;

import reactor.core.publisher.Mono;

@Service
public final class TableService {

    private final TableRepository tableRepository;
    private final UserGateway userGateway;

    public TableService(TableRepository tableRepository, UserGateway userGateway) {
        this.tableRepository = tableRepository;
        this.userGateway = userGateway;
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

                    if (table.getStatus() != GameStatus.WAGER_PLACEMENT) {
                        return Mono.error(ServiceException.badStatus());
                    }

                    UserId userId = table.getPlayer().getUserId();
                    Money bet = Money.of(betAmount);

                    return userGateway.charge(userId, bet)
                            .then(Mono.fromRunnable(() -> table.placeBet(bet)))
                            .then(tableRepository.save(table));
                });

    }

    public Mono<Table> startNewGame(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> {

                    table.start();

                    if (table.getStatus() == GameStatus.FINISHED) {

                        Money payoutAmount = table.calculatePayout();
                        UserId userId = table.getPlayer().getUserId();

                        return userGateway.payout(userId, payoutAmount)
                                .then(tableRepository.save(table));
                    }
                    return tableRepository.save(table);
                });
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

                    Money payoutAmount = table.calculatePayout();

                    if (payoutAmount.getAmount() > 0) {
                        UserId userId = table.getPlayer().getUserId();
                        return userGateway.payout(userId, payoutAmount)
                                .then(tableRepository.save(table));
                    }

                    return tableRepository.save(table);
                });
    }

    public Mono<Void> closeTable(String tableId) {
        return tableRepository.findById(TableId.of(tableId))
                .switchIfEmpty(Mono.error(ServiceException.tableNotFoundException()))
                .flatMap(table -> tableRepository.deleteById(table.getId()));
    }
}
