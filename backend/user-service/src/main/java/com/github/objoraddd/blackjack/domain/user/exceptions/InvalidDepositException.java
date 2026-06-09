package com.github.objoraddd.domain.user.exceptions;

import com.github.objoraddd.domain.exceptions.DomainException;

public class InvalidDepositException extends DomainException {
    public InvalidDepositException(String message) {
        super(message);
    }

    public static InvalidDepositException zeroDeposit() {
        return new InvalidDepositException("Deposit amount cannot be zero");
    }

    public static InvalidDepositException smallDeposit() {
        return new InvalidDepositException("Deposit amount is less than minimum deposit");
    }

}
