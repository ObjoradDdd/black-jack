package com.github.objoraddd.blackjack.domain.gateway;

import com.github.objoraddd.blackjack.domain.table.valueobjects.Money;
import com.github.objoraddd.blackjack.domain.table.valueobjects.UserId;

public interface UserGateway {
    Void holdBalance(UserId userId);

    Void approveHold(UserId userId);

    Void rejectHold(UserId userId);

    Void payout(UserId userId, Money amount);
}
