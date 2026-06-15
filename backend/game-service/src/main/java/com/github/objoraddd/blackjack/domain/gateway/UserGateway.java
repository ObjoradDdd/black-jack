package com.github.objoraddd.blackjack.domain.gateway;

import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;

import reactor.core.publisher.Mono;

public interface UserGateway {
    Mono<Money> getUserBalance(UserId userId);
}
