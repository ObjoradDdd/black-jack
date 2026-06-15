package com.github.objoraddd.blackjack.infrastructure.persistence.table;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.entities.Deck;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Hand;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Rank;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Suit;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Username;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameStatus;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class TableDataMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TableEntity toEntity(Table domainTable) {
        String id = Objects.requireNonNull(domainTable.getId(), "Table ID cannot be null").getValue();
        String userId = Objects.requireNonNull(domainTable.getPlayer().getUserId(), "User ID cannot be null")
                .getValue();
        String username = Objects.requireNonNull(domainTable.getPlayer().getUsername(), "Username cannot be null")
                .getValue();

        Long balance = Objects.requireNonNull(domainTable.getPlayer().getBalance(), "Balance cannot be null")
                .getAmount();

        String playerCardsJson = serializeCards(domainTable.getPlayer().getHand().getCards());
        String dealerCardsJson = serializeCards(domainTable.getDealerHand().getCards());
        String deckCardsString = serializeDeck(domainTable.getDeck().getCards());

        return new TableEntity(
                id,
                userId,
                username,
                balance,
                domainTable.getStatus().name(),
                domainTable.getResult() != null ? domainTable.getResult().name() : null,
                domainTable.getPlayer().getBet().getAmount(),
                playerCardsJson,
                dealerCardsJson,
                deckCardsString);
    }

    public static Table toDomain(TableEntity entity) {
        String id = Objects.requireNonNull(entity.id(), "Entity ID cannot be null");
        String userId = Objects.requireNonNull(entity.userId(), "Entity User ID cannot be null");
        String username = Objects.requireNonNull(entity.username(), "Entity Username cannot be null");
        String statusStr = Objects.requireNonNull(entity.status(), "Entity Status cannot be null");
        Long balance = Objects.requireNonNull(entity.balance(), "Entity Balance cannot be null");

        List<Card> playerCardList = deserializeCards(entity.playerCards());
        List<Card> dealerCardList = deserializeCards(entity.dealerCards());

        Player player = new Player(UserId.of(userId), Username.of(username), Money.of(balance));
        if (entity.betAmount() != null && entity.betAmount() > 0) {
            player.placeBet(Money.of(entity.betAmount()));
        }

        player.buildHand(playerCardList);

        Hand dealerHand = Hand.rebuildFromList(dealerCardList);

        List<Card> deckCardList = deserializeDeckString(entity.deckCards());
        Deck deck = Deck.rebuildFromList(deckCardList);

        GameStatus status = GameStatus.valueOf(statusStr);
        GameResult result = entity.result() != null ? GameResult.valueOf(entity.result()) : null;

        return new Table(
                TableId.of(id),
                player,
                deck,
                dealerHand,
                status,
                result);
    }

    private static String serializeCards(List<Card> cards) {
        try {
            if (cards == null)
                return "[]";
            List<String> cardNames = cards.stream()
                    .map(c -> c.getRank().name() + "_" + c.getSuit().name())
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(cardNames);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String serializeDeck(List<Card> cards) {
        if (cards == null || cards.isEmpty())
            return "";
        return cards.stream()
                .map(c -> c.getRank().name() + "_" + c.getSuit().name())
                .collect(Collectors.joining(","));
    }

    private static List<Card> deserializeCards(String json) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            List<String> cardNames = objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
            return cardNames.stream()
                    .map(TableDataMapper::parseCardString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Card parseCardString(String cardString) {
        String[] parts = cardString.split("_");
        return new Card(Suit.valueOf(parts[1]), Rank.valueOf(parts[0]));
    }

    private static List<Card> deserializeDeckString(String deckString) {
        if (deckString == null || deckString.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(deckString.split(","))
                .map(TableDataMapper::parseCardString)
                .collect(Collectors.toList());
    }
}