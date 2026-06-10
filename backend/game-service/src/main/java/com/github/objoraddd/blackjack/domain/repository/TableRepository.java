package com.github.objoraddd.blackjack.domain.repository;

import com.github.objoraddd.blackjack.domain.Table.Table;
import com.github.objoraddd.blackjack.domain.Table.valueobjects.TableId;

import reactor.core.publisher.Mono;

public interface TableRepository {
    Mono<Table> findById(TableId tableId);

    Mono<Table> save(Table table);

    Mono<Void> deleteById(TableId tableId);
}