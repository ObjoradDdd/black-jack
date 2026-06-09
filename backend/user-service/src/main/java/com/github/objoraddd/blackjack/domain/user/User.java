package com.github.objoraddd.domain.user;

import com.github.objoraddd.domain.user.exceptions.InvalidDepositException;
import com.github.objoraddd.domain.user.exceptions.InvalidMoneyException;
import com.github.objoraddd.domain.user.exceptions.InvalidSpendingException;
import com.github.objoraddd.domain.user.exceptions.InvalidWinException;
import com.github.objoraddd.domain.user.valueobjects.Email;
import com.github.objoraddd.domain.user.valueobjects.Money;
import com.github.objoraddd.domain.user.valueobjects.UserId;
import com.github.objoraddd.domain.user.valueobjects.Username;

public class User {
    private final UserId id;
    private Username username;
    private Email email;
    private Money balance;

    public User(UserId id, Username username, Email email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.balance = Money.zero();
    }

    public void deposit(Money amount, Money minimumDeposit) {
        if (amount.isZero()) {
            throw InvalidDepositException.zeroDeposit();
        }
        if (amount.isLessThan(minimumDeposit)) {
            throw InvalidDepositException.smallDeposit();
        }
        this.balance = this.balance.add(amount);
    }

    public void win(Money amount) {
        if (amount.isZero()) {
            throw InvalidWinException.zeroWin();
        }
        this.balance = this.balance.add(amount);
    }

    public void spend(Money amount) {
        if (amount.isZero()) {
            throw InvalidSpendingException.zeroSpending();
        }

        try {
            this.balance = this.balance.subtract(amount);
        } catch (InvalidMoneyException e) {
            throw InvalidSpendingException.tooBigSpending();
        }
    }

    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Money getBalance() {
        return balance;
    }
}
