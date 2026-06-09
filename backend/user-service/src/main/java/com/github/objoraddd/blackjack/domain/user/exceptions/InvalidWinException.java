package com.github.objoraddd.domain.user.exceptions;

import com.github.objoraddd.domain.exceptions.DomainException;

public class InvalidWinException extends DomainException {
    public InvalidWinException(String message) {
        super(message);
    }

    public static InvalidWinException zeroWin() {
        return new InvalidWinException("Winning amount cannot be zero");
    }

}
