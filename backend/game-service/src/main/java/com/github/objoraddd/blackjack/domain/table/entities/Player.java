package com.github.objoraddd.blackjack.domain.table.entities;

import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidHandException;
import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidPlayerException;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Hand;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Username;

public final class Player {
    private final UserId userId;
    private final Username username;
    private final Money initialBalance;

    private Money balance;
    private Hand hand;
    private Money bet;

    public static Player createNewPlayer(UserId userId, Username username, Money balance) {
        return new Player(userId, username, balance, balance);
    }

    public static Player rebuildFromState(UserId userId, Username username, Money balance, Money initialBalance,
            Hand hand, Money bet) {
        Player player = new Player(userId, username, balance, initialBalance);
        player.hand = hand;
        player.bet = bet;
        return player;
    }

    private Player(UserId userId, Username username, Money balance, Money initialBalance) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.initialBalance = initialBalance;
        this.hand = Hand.emptyHand();
        this.bet = Money.zero();
    }

    public void placeBet(Money betAmount) {
        if (betAmount == null || betAmount.isZero()) {
            throw InvalidPlayerException.zeroBetException();
        } else if (betAmount.isGreaterThan(balance)) {
            throw InvalidPlayerException.notEnoughMoneyException();
        }
        this.balance = Money.of(balance.getAmount() - betAmount.getAmount());
        this.bet = betAmount;
    }

    public void topUpBalance(Money amount) {
        if (amount != null && !amount.isZero()) {
            this.balance = Money.of(this.balance.getAmount() + amount.getAmount());
        }
        this.bet = Money.zero();
    }

    public void clearBet() {
        this.bet = Money.zero();
    }

    public void receiveCard(Card card) {
        if (card == null) {
            throw InvalidHandException.nullCardException();
        }
        this.hand = this.hand.addCard(card);
    }

    public void clearHand() {
        this.hand = Hand.emptyHand();
    }

    public Hand getHand() {
        return hand;
    }

    public UserId getUserId() {
        return userId;
    }

    public Username getUsername() {
        return username;
    }

    public Money getBet() {
        return bet;
    }

    public Money getBalance() {
        return balance;
    }

    public Money getInitialBalance() {
        return initialBalance;
    }
}