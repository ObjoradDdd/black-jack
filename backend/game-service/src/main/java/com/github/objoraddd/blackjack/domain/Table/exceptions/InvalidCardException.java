package com.github.objoraddd.blackjack.domain.Table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidCardException extends DomainException {
    public InvalidCardException(String message) {
        super(message);
    }

    public static InvalidCardException badCardException() {
        return new InvalidCardException("Invalid card provided.");
    }

}
