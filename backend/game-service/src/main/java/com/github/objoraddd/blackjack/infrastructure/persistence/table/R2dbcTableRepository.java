package com.github.objoraddd.blackjack.infrastructure.persistence.table;

import com.github.objoraddd.blackjack.domain.repository.TableRepository;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
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
        String sql = """
                SELECT id, user_id, username, balance, status, result, bet_amount, player_cards, dealer_cards, deck_cards
                FROM blackjack_tables
                WHERE id = :id
                FOR UPDATE
                """;

        return databaseClient.sql(sql)
                .bind("id", Objects.requireNonNull(id.getValue(), "Table ID cannot be null"))
                .map((row, metadata) -> new TableEntity(
                        Objects.requireNonNull(row.get("id", String.class), "Table ID cannot be null"),
                        Objects.requireNonNull(row.get("user_id", String.class), "User ID cannot be null"),
                        Objects.requireNonNull(row.get("username", String.class), "Username cannot be null"),
                        Objects.requireNonNull(row.get("balance", Long.class), "Balance cannot be null"),
                        Objects.requireNonNull(row.get("status", String.class), "Status cannot be null"),
                        Objects.requireNonNull(row.get("result", String.class), "Result cannot be null"),
                        Objects.requireNonNull(row.get("bet_amount", Long.class), "Bet amount cannot be null"),
                        Objects.requireNonNull(row.get("player_cards", String.class), "Player cards cannot be null"),
                        Objects.requireNonNull(row.get("dealer_cards", String.class), "Dealer cards cannot be null"),
                        Objects.requireNonNull(row.get("deck_cards", String.class), "Deck cards cannot be null")))
                .one()
                .map(TableDataMapper::toDomain);
    }

    @Override
    public Mono<Table> create(Table table) {
        String sql = """
                INSERT INTO blackjack_tables (id, user_id, username, balance, status, result, bet_amount, player_cards, dealer_cards, deck_cards)
                VALUES (:id, :userId, :username, :balance, :status, :result, :betAmount, :playerCards::jsonb, :dealerCards::jsonb, :deckCards)
                """;

        return executeWrite(sql, table);
    }

    @Override
    public Mono<Table> update(Table table) {
        String sql = """
                UPDATE blackjack_tables
                SET status = :status,
                    result = :result,
                    bet_amount = :betAmount,
                    player_cards = :playerCards::jsonb,
                    dealer_cards = :dealerCards::jsonb,
                    deck_cards = :deckCards,
                    balance = :balance
                WHERE id = :id
                """;

        return executeWrite(sql, table);
    }

    @Override
    public Mono<Void> deleteById(TableId id) {
        return databaseClient.sql("DELETE FROM blackjack_tables WHERE id = :id")
                .bind("id", Objects.requireNonNull(id.getValue(), "Table ID cannot be null"))
                .then();
    }

    private Mono<Table> executeWrite(String sql, Table table) {
        TableEntity entity = TableDataMapper.toEntity(table);
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(Objects.requireNonNull(sql, "SQL cannot be null"));

        for (Map.Entry<String, Object> entry : getBindMap(entity).entrySet()) {
            spec = (entry.getValue() == null)
                    ? spec.bindNull(Objects.requireNonNull(entry.getKey(), "Bind key cannot be null"), String.class)
                    : spec.bind(Objects.requireNonNull(entry.getKey(), "Bind key cannot be null"),
                            Objects.requireNonNull(entry.getValue(), "Bind value cannot be null"));
        }

        return spec.then().thenReturn(table);
    }

    private Map<String, Object> getBindMap(TableEntity entity) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", entity.id());
        map.put("userId", entity.userId());
        map.put("username", entity.username());
        map.put("balance", entity.balance());
        map.put("status", entity.status());
        map.put("result", entity.result());
        map.put("betAmount", entity.betAmount());
        map.put("playerCards", entity.playerCards());
        map.put("dealerCards", entity.dealerCards());
        map.put("deckCards", entity.deckCards());
        return map;
    }
}