package com.wallet.youssef.wallet_service.web.dto;

import java.math.BigDecimal;

public class MoneyRequest {
    public BigDecimal amount;
    public String reference;    // اختياري
    public String description;  // اختياري
}
