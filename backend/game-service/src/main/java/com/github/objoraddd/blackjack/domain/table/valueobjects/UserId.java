package com.github.objoraddd.blackjack.domain.table.valueobjects;

public final class UserId {
    private String value;

    private UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}