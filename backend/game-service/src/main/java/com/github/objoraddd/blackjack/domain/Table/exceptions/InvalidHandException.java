package com.github.objoraddd.blackjack.domain.Table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidHandException extends DomainException {
    public InvalidHandException(String message) {
        super(message);
    }

    public static InvalidHandException nullCardListException() {
        return new InvalidHandException("Card list cannot be null.");
    }

    public static InvalidHandException nullCardException() {
        return new InvalidHandException("Adding card cannot be null.");
    }

}
