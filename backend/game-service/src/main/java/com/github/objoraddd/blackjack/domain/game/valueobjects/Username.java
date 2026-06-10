package com.github.objoraddd.blackjack.domain.game.valueobjects;

public class Username {
    private String value;

    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
