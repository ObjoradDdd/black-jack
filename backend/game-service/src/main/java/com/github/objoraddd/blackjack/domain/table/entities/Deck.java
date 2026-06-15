package com.github.objoraddd.blackjack.domain.table.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidDeckException;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.table.valueobjects.DeckCount;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Rank;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Suit;

public final class Deck {
    private List<Card> cards;
    private final DeckCount deckCount;

    public static Deck createMultiDeck(DeckCount deckCount) {
        if (deckCount.getValue() <= 0) {
            throw InvalidDeckException.invalidCardNumberDeckException();
        }
        List<Card> newCards = generateCards(deckCount);
        return new Deck(newCards, deckCount);
    }

    public static Deck rebuildFromList(List<Card> cards, DeckCount deckCount) {
        if (cards == null || cards.isEmpty()) {
            throw InvalidDeckException.emptyDeckException();
        }
        return new Deck(cards, deckCount);
    }

    private Deck(List<Card> cards, DeckCount deckCount) {
        this.cards = new ArrayList<>(cards);
        this.deckCount = deckCount;
    }

    public boolean needsShuffling() {
        int totalInitialCards = this.deckCount.getValue() * 52;
        return this.cards.size() < (totalInitialCards * 0.25);
    }

    public void shuffleAndReset() {
        List<Card> newCards = generateCards(deckCount);
        this.cards = newCards;
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

    public List<Card> getCards() {
        return cards;
    }

    public DeckCount getDeckCount() {
        return deckCount;
    }

    static private List<Card> generateCards(DeckCount deckCount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < deckCount.getValue(); i++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        Collections.shuffle(cards);
        return cards;
    }
}