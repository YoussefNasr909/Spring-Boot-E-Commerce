package com.wallet.youssef.wallet_service.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WalletResponse {
    public Long id;
    public String ownerEmail;
    public BigDecimal balance;
    public LocalDateTime createdAt;

    public WalletResponse(Long id, String ownerEmail, BigDecimal balance, LocalDateTime createdAt) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.balance = balance;
        this.createdAt = createdAt;
    }
}
