package com.github.objoraddd.blackjack.domain.gateway;

import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;

import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Void> charge(UserId userId, Money amount);

    Mono<Void> payout(UserId userId, Money amount);
}
