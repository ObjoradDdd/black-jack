package com.github.objoraddd.blackjack.domain.Table.valueobjects;

public class TableId {
    private String value;

    private TableId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("TableId cannot be null or empty");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TableId of(String value) {
        return new TableId(value);
    }
}
