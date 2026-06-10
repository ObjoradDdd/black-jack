package com.github.objoraddd.blackjack.domain.gateway;

import com.github.objoraddd.blackjack.domain.Table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.UserId;

import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Void> charge(UserId userId, Money amount);

    Mono<Void> payout(UserId userId, Money amount);
}
