package com.shop.youssef.shop_service.client.fallback;

import com.shop.youssef.shop_service.client.WalletClient;
import com.shop.youssef.shop_service.client.dto.MoneyRequest;
import com.shop.youssef.shop_service.client.dto.WalletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class WalletClientFallback implements WalletClient {

    @Override
    public WalletResponse withdraw(Long walletId, MoneyRequest request) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Wallet service unavailable. Please try again."
        );
    }

    @Override
    public WalletResponse deposit(Long walletId, MoneyRequest request) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Wallet service unavailable. Please try again."
        );
    }
}
