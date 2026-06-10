package com.github.objoraddd.blackjack.domain.game.valueobjects;

import com.github.objoraddd.blackjack.domain.exceptions.DomainException;

public class InvalidMoneyException extends DomainException {
    public InvalidMoneyException(String message) {
        super(message);
    }

    public static InvalidMoneyException negativeAmount() {
        return new InvalidMoneyException("Amount cannot be negative");
    }

}
