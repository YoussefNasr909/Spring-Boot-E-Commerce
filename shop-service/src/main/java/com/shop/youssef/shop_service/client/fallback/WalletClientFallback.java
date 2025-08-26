package com.shop.youssef.shop_service.client.fallback;

import com.shop.youssef.shop_service.client.WalletClient;
import com.shop.youssef.shop_service.client.dto.MoneyRequest;
import com.shop.youssef.shop_service.client.dto.WalletResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletClientFallback implements WalletClient {

    @Override
    public WalletResponse withdraw(Long walletId, MoneyRequest request) {
        throw new IllegalStateException("Wallet service unavailable. Please try again.");
    }

    @Override
    public WalletResponse deposit(Long walletId, MoneyRequest request) {
        throw new IllegalStateException("Wallet service unavailable. Please try again.");
    }
}
