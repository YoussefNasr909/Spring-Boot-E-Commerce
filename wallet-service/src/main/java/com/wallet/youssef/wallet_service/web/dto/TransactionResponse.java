package com.wallet.youssef.wallet_service.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    public Long id;
    public String type; // "DEPOSIT" أو "WITHDRAW"
    public BigDecimal amount;
    public String reference;
    public String description;
    public LocalDateTime createdAt;

    public TransactionResponse(Long id, String type, BigDecimal amount, String reference, String description, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.reference = reference;
        this.description = description;
        this.createdAt = createdAt;
    }
}
