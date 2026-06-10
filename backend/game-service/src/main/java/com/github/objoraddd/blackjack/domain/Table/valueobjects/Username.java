package com.github.objoraddd.blackjack.domain.Table.valueobjects;

public final class Username {
    private String value;

    private Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Username of(String value) {
        return new Username(value);
    }
}
