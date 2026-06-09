package com.github.objoraddd.domain.valueobjects;

public final class Email {
    private final String value;

    public Email(String value) {
        if (!value.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
