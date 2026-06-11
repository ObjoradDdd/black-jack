package com.github.objoraddd.blackjack.domain.table.valueobjects;

import java.util.ArrayList;
import java.util.List;

import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidHandException;

public final class Hand {
    private final List<Card> cards;

    public Hand(List<Card> cards) {
        if (cards == null) {
            throw InvalidHandException.nullCardListException();
        }
        this.cards = List.copyOf(cards);
    }

    public static Hand emptyHand() {
        return new Hand(List.of());
    }

    public List<Card> getCards() {
        return cards;
    }

    public Hand addCard(Card card) {
        if (card == null) {
            throw InvalidHandException.nullCardException();
        }

        List<Card> newCards = new ArrayList<>(this.cards);
        newCards.add(card);
        return new Hand(newCards);
    }

    public int calculateScore() {
        int score = 0;
        int aceCount = 0;

        for (Card card : cards) {
            score += card.getRank().getValue();
            if (card.getRank().isAce()) {
                aceCount++;
            }
        }

        while (score > 21 && aceCount > 0) {
            score -= 10;
            aceCount--;
        }

        return score;
    }

    public boolean isBusted() {
        return calculateScore() > 21;
    }

    public boolean isBlackjack() {
        return cards.size() == 2 && calculateScore() == 21;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Hand hand = (Hand) o;

        return cards.equals(hand.cards);
    }

    @Override
    public int hashCode() {
        return cards.hashCode();
    }
}