package com.github.objoraddd.blackjack.domain.Table.entities;

import com.github.objoraddd.blackjack.domain.Table.exceptions.InvalidHandException;
import com.github.objoraddd.blackjack.domain.Table.exceptions.InvalidPlayerException;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Hand;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.Username;

public final class Player {
    private final UserId userId;
    private final Username username;

    private Hand hand;
    private Money bet;

    public Player(UserId userId, Username username) {
        this.userId = userId;
        this.username = username;
        this.hand = Hand.emptyHand();
        this.bet = Money.zero();
    }

    public void placeBet(Money betAmount) {
        if (betAmount == null || betAmount.isZero()) {
            throw InvalidPlayerException.zeroBetException();
        }
        this.bet = betAmount;
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
}