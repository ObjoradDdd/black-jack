package com.github.objoraddd.blackjack.application.exceptions;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }

    public static ServiceException tableNotFoundException() {
        return new ServiceException("Table not found");
    }

    public static ServiceException badStatus() {
        return new ServiceException("Bad status");
    }

}
