package com.github.objoraddd.blackjack.domain.table.events;

import java.time.Instant;

import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;

public record BetEvent(
        TableId tableId,
        UserId userId,
        Money payoutAmount,
        Instant occurredOn) implements DomainEvent {

    @Override
    public String aggregateId() {
        return tableId.getValue();
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }
}
