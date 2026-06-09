package com.github.objoraddd.domain.user.exceptions;

import com.github.objoraddd.domain.exceptions.DomainException;

public class InvalidMoneyException extends DomainException {
    public InvalidMoneyException(String message) {
        super(message);
    }

    public static InvalidMoneyException negativeAmount() {
        return new InvalidMoneyException("Amount cannot be negative");
    }

}
