package com.github.objoraddd.blackjack.domain.Table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidTableException extends DomainException {
    public InvalidTableException(String message) {
        super(message);
    }

    public static InvalidTableException InvalidMoveException() {
        return new InvalidTableException("Invalid move for the current game state.");
    }

    public static InvalidTableException GameNotStartedException() {
        return new InvalidTableException("Game has not started yet.");
    }

    public static InvalidTableException GameAlreadyFinishedException() {
        return new InvalidTableException("Game has already finished.");
    }

}
