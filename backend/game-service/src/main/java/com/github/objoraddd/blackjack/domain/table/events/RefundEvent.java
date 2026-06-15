package com.github.objoraddd.blackjack.domain.table.events;

import java.time.Instant;

import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;

public record RefundEvent(
        TableId tableId,
        UserId userId,
        Money amount,
        Instant occurredOn) implements DomainEvent {

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public TableId aggregateId() {
        return tableId;
    }
}