package com.github.objoraddd.blackjack.domain.game.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidTableException extends DomainException {
    public InvalidTableException(String message) {
        super(message);
    }

    public static InvalidTableException InvalidMoveException() {
        return new InvalidTableException("Invalid move for the current game state.");
    }

}
