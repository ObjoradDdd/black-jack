package com.github.objoraddd.blackjack.application.outbox;

import com.github.objoraddd.blackjack.domain.table.events.DomainEvent;
import reactor.core.publisher.Mono;

public interface OutboxPublisher {
    Mono<DomainEvent> publish(DomainEvent event);
}
