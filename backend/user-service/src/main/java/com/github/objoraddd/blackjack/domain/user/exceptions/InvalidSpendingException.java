package com.github.objoraddd.domain.user.exceptions;

import com.github.objoraddd.domain.exceptions.DomainException;

public class InvalidSpendingException extends DomainException {

    public InvalidSpendingException(String message) {
        super(message);
    }

    public static InvalidSpendingException tooBigSpending() {
        return new InvalidSpendingException("Spend amount cannot be greater than balance");
    }

    public static InvalidSpendingException zeroSpending() {
        return new InvalidSpendingException("Spend amount cannot be zero");
    }
}