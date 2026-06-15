package com.github.objoraddd.blackjack.domain.table.valueobjects;

import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidDeckException;

public final class DeckCount {
    private Integer value;

    private DeckCount(int value) {
        if (value <= 0) {
            throw InvalidDeckException.invalidDeckContException();
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DeckCount of(int value) {
        return new DeckCount(value);
    }
}
