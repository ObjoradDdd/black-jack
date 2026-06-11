package com.github.objoraddd.blackjack.domain.repository;

import com.github.objoraddd.blackjack.domain.table.Table;
import com.github.objoraddd.blackjack.domain.table.valueobjects.TableId;

import reactor.core.publisher.Mono;

public interface TableRepository {
    Mono<Table> findById(TableId tableId);

    Mono<Table> save(Table table);

    Mono<Void> deleteById(TableId tableId);
}