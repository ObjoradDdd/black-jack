package com.github.objoraddd.blackjack.domain.table.events;

import java.time.Instant;

import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;

public interface DomainEvent {
    Instant occurredOn();

    TableId aggregateId();
}
