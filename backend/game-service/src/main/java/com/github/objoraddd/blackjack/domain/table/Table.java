package com.github.objoraddd.blackjack.domain.table;

import com.github.objoraddd.blackjack.domain.table.entities.Deck;
import com.github.objoraddd.blackjack.domain.table.entities.Player;
import com.github.objoraddd.blackjack.domain.table.exceptions.InvalidTableException;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameResult;
import com.github.objoraddd.blackjack.domain.table.valueobjects.GameStatus;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Hand;
import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;

public final class Table {
    private final TableId id;
    private final Player player;
    private final Deck deck;

    private Hand dealerHand;
    private GameStatus status;
    private GameResult result;

    public Table(TableId id, Player player) {
        this.id = id;
        this.player = player;
        this.deck = Deck.createStandardDeck();
        this.dealerHand = Hand.emptyHand();
        this.status = GameStatus.WAGER_PLACEMENT;
        this.result = null;
    }

    public Table(TableId id, Player player, Deck deck, Hand dealerHand, GameStatus status, GameResult result) {
        this.id = id;
        this.player = player;
        this.deck = deck;
        this.dealerHand = dealerHand;
        this.status = status;
        this.result = result;
    }

    public void placeBet(Money betAmount) {
        if (this.status != GameStatus.WAGER_PLACEMENT) {
            throw InvalidTableException.InvalidMoveException();
        }
        this.player.placeBet(betAmount);
    }

    public void start() {
        if (this.status != GameStatus.WAGER_PLACEMENT) {
            throw InvalidTableException.InvalidMoveException();
        }
        if (this.player.getBet().isZero()) {
            throw InvalidTableException.GameNotStartedException();
        }

        this.player.clearHand();
        this.dealerHand = Hand.emptyHand();
        this.result = null;

        this.player.receiveCard(deck.drawCard());
        this.player.receiveCard(deck.drawCard());

        this.dealerHand = this.dealerHand.addCard(deck.drawCard());
        this.dealerHand = this.dealerHand.addCard(deck.drawCard());

        if (this.player.getHand().isBlackjack()) {
            this.status = GameStatus.FINISHED;
            determineResult();
        } else {
            this.status = GameStatus.PLAYER_TURN;
        }
    }

    public void playerHit() {
        if (this.status != GameStatus.PLAYER_TURN) {
            throw InvalidTableException.InvalidMoveException();
        }

        this.player.receiveCard(deck.drawCard());

        if (this.player.getHand().isBusted()) {
            this.status = GameStatus.FINISHED;
            determineResult();
        }
    }

    public void playerStand() {
        if (this.status != GameStatus.PLAYER_TURN) {
            throw InvalidTableException.InvalidMoveException();
        }

        this.status = GameStatus.DEALER_TURN;
        executeDealerTurn();
    }

    private void executeDealerTurn() {
        while (this.dealerHand.calculateScore() < 17) {
            this.dealerHand = this.dealerHand.addCard(deck.drawCard());
        }

        this.status = GameStatus.FINISHED;
        determineResult();
    }

    private void determineResult() {
        int playerScore = this.player.getHand().calculateScore();
        int dealerScore = this.dealerHand.calculateScore();

        if (this.player.getHand().isBusted()) {
            this.result = GameResult.DEALER_WON;
            return;
        }

        if (this.dealerHand.isBusted()) {
            this.result = GameResult.PLAYER_WON;
            return;
        }

        if (this.player.getHand().isBlackjack() && !this.dealerHand.isBlackjack()) {
            this.result = GameResult.PLAYER_WON;
            return;
        }
        if (!this.player.getHand().isBlackjack() && this.dealerHand.isBlackjack()) {
            this.result = GameResult.DEALER_WON;
            return;
        }

        if (playerScore > dealerScore) {
            this.result = GameResult.PLAYER_WON;
        } else if (playerScore < dealerScore) {
            this.result = GameResult.DEALER_WON;
        } else {
            this.result = GameResult.DRAW;
        }
    }

    public Money calculatePayout() {
        if (this.status != GameStatus.FINISHED) {
            throw InvalidTableException.GameAlreadyFinishedException();
        }

        long betAmount = this.player.getBet().getAmount();

        switch (this.result) {
            case PLAYER_WON:
                return Money.of(betAmount * 2);
            case DRAW:
                return this.player.getBet();
            case DEALER_WON:
            default:
                return Money.zero();
        }
    }

    public TableId getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public Hand getDealerHand() {
        return dealerHand;
    }

    public Deck getDeck() {
        return deck;
    }

    public GameStatus getStatus() {
        return status;
    }

    public GameResult getResult() {
        return result;
    }
}