package com.github.objoraddd.domain.valueobjects;

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
