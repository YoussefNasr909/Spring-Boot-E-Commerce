package com.shop.youssef.shop_service.client;

import com.shop.youssef.shop_service.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wallet-service")
public interface WalletClient {

    @PostMapping("/wallet/{walletId}/withdraw")
    WalletResponse withdraw(@PathVariable("walletId") Long walletId,
                            @RequestBody MoneyRequest request);

    @PostMapping("/wallet/{walletId}/deposit")
    WalletResponse deposit(@PathVariable("walletId") Long walletId,
                           @RequestBody MoneyRequest request);
}
