package com.github.objoraddd.blackjack.infrastructure.persistence.table;

import java.time.Instant;

public record TableEntity(
        String id,
        String userId,
        String username,
        Long balance,
        String status,
        String result,
        Long betAmount,
        String playerCards,
        String dealerCards,
        String deckCards,
        Instant lastUpdated) {
}