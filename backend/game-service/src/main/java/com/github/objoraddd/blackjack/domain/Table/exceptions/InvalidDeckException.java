package com.github.objoraddd.blackjack.domain.Table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidDeckException extends DomainException {
    public InvalidDeckException(String message) {
        super(message);
    }

    public static InvalidDeckException invalidCardNumberDeckException() {
        return new InvalidDeckException("Number of cards in the deck must be greater than zero.");
    }

    public static InvalidDeckException emptyDeckException() {
        return new InvalidDeckException("Number of cards cannot be zero.");
    }

}
