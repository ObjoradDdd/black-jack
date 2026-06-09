package com.github.objoraddd.domain.user.valueobjects;

import java.util.Objects;

import com.github.objoraddd.domain.user.exceptions.InvalidMoneyException;

public final class Money {
    private final Long amount;

    public Money(long amount) {
        if (amount < 0) {
            throw InvalidMoneyException.negativeAmount();
        }
        this.amount = amount;
    }

    public static Money zero() {
        return new Money(0L);
    }

    public boolean isLessThan(Money other) {
        return this.amount < other.getAmount();
    }

    public boolean isGreaterThan(Money other) {
        return this.amount > other.getAmount();
    }

    public boolean isZero() {
        return this.amount == 0L;
    }

    public static Money of(Long amount) {
        return new Money(amount);
    }

    public Long getAmount() {
        return amount;
    }

    public Money add(Money other) {
        return new Money(this.amount + other.getAmount());
    }

    public Money subtract(Money other) {
        if (this.isLessThan(other)) {
            throw InvalidMoneyException.negativeAmount();
        }
        return new Money(this.amount - other.getAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}