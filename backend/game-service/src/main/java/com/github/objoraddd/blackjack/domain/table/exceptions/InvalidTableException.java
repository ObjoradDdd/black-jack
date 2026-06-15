package com.github.objoraddd.blackjack.domain.table.exceptions;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidTableException extends DomainException {
    public InvalidTableException(String message) {
        super(message);
    }

    public static InvalidTableException invalidMoveException() {
        return new InvalidTableException("Invalid move for the current game state.");
    }

    public static InvalidTableException gameNotStartedException() {
        return new InvalidTableException("Game has not started yet.");
    }

    public static InvalidTableException gameAlreadyFinishedException() {
        return new InvalidTableException("Game has already finished.");
    }

    public static InvalidTableException invalidBetAmountException() {
        return new InvalidTableException("Bet amount must be an even number.");
    }

}
