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

    public static ServiceException playerAlreadyInGameException() {
        return new ServiceException("Player is already in a game");
    }

    public static ServiceException userNotFoundException() {
        return new ServiceException("User not found");
    }

    public static ServiceException zeroBalanceException() {
        return new ServiceException("Cannot create table with zero balance");
    }

}
