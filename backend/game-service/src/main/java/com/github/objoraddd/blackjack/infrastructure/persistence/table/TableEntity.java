package com.github.objoraddd.blackjack.infrastructure.persistence.table;

public record TableEntity(
        String id,
        String userId,
        String username,
        String status,
        String result,
        Long betAmount,
        String playerCards,
        String dealerCards,
        String deckCards) {
}