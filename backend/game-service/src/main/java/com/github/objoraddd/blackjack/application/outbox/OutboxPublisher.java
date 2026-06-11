package com.github.objoraddd.blackjack.application.outbox;

import com.github.objoraddd.blackjack.domain.table.events.DomainEvent;

import reactor.core.publisher.Mono;

public interface OutboxPublisher {
    public Mono<Void> publish(DomainEvent message);
}