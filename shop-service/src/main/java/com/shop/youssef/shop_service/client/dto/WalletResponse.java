package com.shop.youssef.shop_service.client.dto;

import java.math.BigDecimal;

public record WalletResponse(Long walletId, BigDecimal balance, String message) {}
