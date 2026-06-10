package com.github.objoraddd.blackjack.domain.game.entities;

import com.github.objoraddd.blackjack.domain.game.exceptions.InvalidHandException;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Card;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Hand;
import com.github.objoraddd.blackjack.domain.game.valueobjects.UserId;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Username;
import com.github.objoraddd.blackjack.domain.game.valueobjects.Money;

public final class Player {
    private final UserId userId; // Исправлено: имя поля с маленькой буквы
    private final Username username;

    private Hand hand;
    private Money bet; // По смыслу лучше назвать bet (ставка), а не money

    public Player(UserId userId, Username username) {
        this.userId = userId;
        this.username = username;
        this.hand = Hand.emptyHand();
        this.bet = Money.zero();
    }

    public void placeBet(Money betAmount) {
        if (betAmount == null || betAmount.isZero()) {
            throw new IllegalArgumentException("Ставка не может быть нулевой или null");
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