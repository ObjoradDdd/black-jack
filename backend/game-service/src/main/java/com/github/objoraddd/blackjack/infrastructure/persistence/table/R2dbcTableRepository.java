package com.github.objoraddd.blackjack.infrastructure.persistence.table;

import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@Repository
public final class R2dbcTableRepository implements TableRepository {

    private final DatabaseClient databaseClient;

    public R2dbcTableRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Table> findById(TableId id) {
        Objects.requireNonNull(id, "Table ID object cannot be null");
        Objects.requireNonNull(id.getValue(), "Table ID value cannot be null");

        String sql = """
                SELECT version, id, user_id, username, balance, initial_balance, status, result, bet_amount, deck_count, player_cards, dealer_cards, deck_cards, last_updated
                FROM blackjack_tables
                WHERE id = :id
                """;

        return databaseClient.sql(sql)
                .bind("id", Objects.requireNonNull(id.getValue(), "Table ID value cannot be null"))
                .map((row, metadata) -> new TableEntity(
                        Objects.requireNonNull(row.get("version", Long.class), "Version cannot be null"),
                        Objects.requireNonNull(row.get("id", String.class), "Table ID cannot be null"),
                        Objects.requireNonNull(row.get("user_id", String.class), "User ID cannot be null"),
                        Objects.requireNonNull(row.get("username", String.class), "Username cannot be null"),
                        Objects.requireNonNull(row.get("balance", Long.class), "Balance cannot be null"),
                        Objects.requireNonNull(row.get("initial_balance", Long.class),
                                "Initial balance cannot be null"),
                        Objects.requireNonNull(row.get("status", String.class), "Status cannot be null"),
                        row.get("result", String.class),
                        Objects.requireNonNull(row.get("bet_amount", Long.class), "Bet amount cannot be null"),
                        Objects.requireNonNull(row.get("deck_count", Integer.class), "Deck count cannot be null"),
                        Objects.requireNonNull(row.get("player_cards", String.class), "Player cards cannot be null"),
                        Objects.requireNonNull(row.get("dealer_cards", String.class), "Dealer cards cannot be null"),
                        Objects.requireNonNull(row.get("deck_cards", String.class), "Deck cards cannot be null"),
                        Objects.requireNonNull(row.get("last_updated", Instant.class), "Last updated cannot be null")))
                .one()
                .map(TableDataMapper::toDomain);
    }

    @Override
    public Mono<Table> create(Table table) {
        Objects.requireNonNull(table, "Table cannot be null");

        String sql = """
                INSERT INTO blackjack_tables (id, user_id, username, balance, initial_balance, status, result, bet_amount, deck_count, player_cards, dealer_cards, deck_cards, last_updated, version)
                VALUES (:id, :userId, :username, :balance, :initialBalance, :status, :result, :betAmount, :deckCount, :playerCards::jsonb, :dealerCards::jsonb, :deckCards, :lastUpdated, 0)
                """;

        TableEntity entity = TableDataMapper.toEntity(table);
        return executeSpecWithBinds(sql, getBindMap(entity)).then().thenReturn(table);
    }

    @Override
    public Mono<Table> update(Table table) {
        Objects.requireNonNull(table, "Table cannot be null");

        String sql = """
                UPDATE blackjack_tables
                SET status = :status,
                    result = :result,
                    bet_amount = :betAmount,
                    player_cards = :playerCards::jsonb,
                    dealer_cards = :dealerCards::jsonb,
                    deck_cards = :deckCards,
                    balance = :balance,
                    last_updated = :lastUpdated,
                    version = :nextVersion
                WHERE id = :id AND version = :currentVersion
                """;

        TableEntity entity = TableDataMapper.toEntity(table);
        long currentVersion = table.getVersion();
        long nextVersion = currentVersion + 1;

        Map<String, Object> bindMap = getBindMap(entity);

        bindMap.put("currentVersion", currentVersion);
        bindMap.put("nextVersion", nextVersion);

        return executeSpecWithBinds(sql, bindMap)
                .fetch()
                .rowsUpdated()
                .flatMap(rowsUpdated -> {
                    if (rowsUpdated == 0) {
                        return Mono.error(new OptimisticLockingFailureException(
                                "Table concurrent modification detected for ID: " + table.getId().getValue()));
                    }
                    return Mono.just(Table.rebuildFromState(
                            table.getId(),
                            table.getPlayer(),
                            table.getDeck(),
                            table.getDealerHand(),
                            table.getStatus(),
                            table.getResult(),
                            nextVersion));
                });
    }

    @Override
    public Mono<Void> deleteById(TableId id) {
        Objects.requireNonNull(id, "Table ID object cannot be null");
        Objects.requireNonNull(id.getValue(), "Table ID value cannot be null");

        return databaseClient.sql("DELETE FROM blackjack_tables WHERE id = :id")
                .bind("id", Objects.requireNonNull(id.getValue(), "Table ID value cannot be null"))
                .then();
    }

    private DatabaseClient.GenericExecuteSpec executeSpecWithBinds(String sql, Map<String, Object> bindMap) {
        DatabaseClient.GenericExecuteSpec spec = databaseClient
                .sql(Objects.requireNonNull(sql, "SQL query cannot be null"));
        for (Map.Entry<String, Object> entry : bindMap.entrySet()) {
            spec = (entry.getValue() == null)
                    ? spec.bindNull(Objects.requireNonNull(entry.getKey(), "Bind key cannot be null"), String.class)
                    : spec.bind(Objects.requireNonNull(entry.getKey(), "Bind key cannot be null"),
                            Objects.requireNonNull(entry.getValue(), "Bind value cannot be null"));
        }
        return spec;
    }

    private Map<String, Object> getBindMap(TableEntity entity) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", Objects.requireNonNull(entity.id(), "Entity ID cannot be null"));
        map.put("userId", Objects.requireNonNull(entity.userId(), "Entity User ID cannot be null"));
        map.put("username", Objects.requireNonNull(entity.username(), "Entity Username cannot be null"));
        map.put("balance", Objects.requireNonNull(entity.balance(), "Entity Balance cannot be null"));
        map.put("initialBalance",
                Objects.requireNonNull(entity.initialBalance(), "Entity Initial Balance cannot be null"));
        map.put("status", Objects.requireNonNull(entity.status(), "Entity Status cannot be null"));
        map.put("result", entity.result());
        map.put("betAmount", Objects.requireNonNull(entity.betAmount(), "Entity Bet Amount cannot be null"));
        map.put("playerCards", Objects.requireNonNull(entity.playerCards(), "Entity Player Cards cannot be null"));
        map.put("dealerCards", Objects.requireNonNull(entity.dealerCards(), "Entity Dealer Cards cannot be null"));
        map.put("deckCards", Objects.requireNonNull(entity.deckCards(), "Entity Deck Cards cannot be null"));
        map.put("lastUpdated", Objects.requireNonNull(entity.lastUpdated(), "Entity Last Updated cannot be null"));
        map.put("deckCount", entity.deckCount());
        return map;
    }
}