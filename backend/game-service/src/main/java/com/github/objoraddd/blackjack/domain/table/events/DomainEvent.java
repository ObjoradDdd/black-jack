package com.github.objoraddd.blackjack.domain.table.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();

    String aggregateId();
}
