package com.github.objoraddd.blackjack.domain.table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidPlayerException extends DomainException {
    public InvalidPlayerException(String message) {
        super(message);
    }

    public static InvalidPlayerException zeroBetException() {
        return new InvalidPlayerException("Bet amount must be greater than zero.");
    }

    public static InvalidPlayerException notEnoughMoneyException() {
        return new InvalidPlayerException("Not enough money to place the bet.");
    }
}
