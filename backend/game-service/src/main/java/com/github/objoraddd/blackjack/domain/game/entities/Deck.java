package com.github.objoraddd.blackjack.domain.game.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.objoraddd.blackjack.domain.game.exceptions.InvalidDeckException;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Rank;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Suit;

public final class Deck {
    private final List<Card> cards;

    private Deck(List<Card> cards) {
        this.cards = List.copyOf(cards);
    }

    public static Deck createStandardDeck() {
        List<Card> newCards = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                newCards.add(new Card(suit, rank));
            }
        }
        Collections.shuffle(newCards);
        return new Deck(newCards);
    }

    public static Deck createMultiDeck(int deckCount) {
        if (deckCount <= 0) {
            throw InvalidDeckException.invalidCardNumberDeckException();
        }
        List<Card> newCards = new ArrayList<>();
        for (int i = 0; i < deckCount; i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    newCards.add(new Card(suit, rank));
                }
            }
        }
        Collections.shuffle(newCards);
        return new Deck(newCards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            throw InvalidDeckException.emptyDeckException();
        }
        return cards.remove(cards.size() - 1);
    }

    public int cardsLeft() {
        return cards.size();
    }

}
