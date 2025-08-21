package com.wallet.youssef.wallet_service.web.dto;

import java.math.BigDecimal;

public class BalanceResponse {
    public Long walletId;
    public BigDecimal balance;

    public BalanceResponse(Long walletId, BigDecimal balance) {
        this.walletId = walletId;
        this.balance = balance;
    }
}
