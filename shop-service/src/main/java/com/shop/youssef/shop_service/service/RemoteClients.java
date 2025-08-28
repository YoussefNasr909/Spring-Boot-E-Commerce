package com.shop.youssef.shop_service.service;

import com.shop.youssef.shop_service.client.InventoryClient;
import com.shop.youssef.shop_service.client.WalletClient;
import com.shop.youssef.shop_service.client.dto.ConsumeRequest;
import com.shop.youssef.shop_service.client.dto.MoneyRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckRequest;
import com.shop.youssef.shop_service.client.dto.StockCheckResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RemoteClients {

    private final InventoryClient inventory;
    private final WalletClient wallet;

    public RemoteClients(InventoryClient inventory, WalletClient wallet) {
        this.inventory = inventory;
        this.wallet = wallet;
    }

    /* ===== Inventory ===== */

    @CircuitBreaker(name = "inventory", fallbackMethod = "checkFallback")
    public List<StockCheckResponse> inventoryCheck(List<StockCheckRequest> reqs) {
        return inventory.check(reqs);
    }

    public List<StockCheckResponse> checkFallback(List<StockCheckRequest> reqs, Throwable ex) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable. Please try again."
        );
    }

    @CircuitBreaker(name = "inventory", fallbackMethod = "consumeFallback")
    public void inventoryConsume(List<ConsumeRequest> reqs) {
        inventory.consume(reqs);
    }

    public void consumeFallback(List<ConsumeRequest> reqs, Throwable ex) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory service unavailable. Please try again."
        );
    }

    /* ===== Wallet ===== */

    @CircuitBreaker(name = "wallet", fallbackMethod = "withdrawFallback")
    public void walletWithdraw(Long walletId, MoneyRequest req) {
        wallet.withdraw(walletId, req);
    }

    public void withdrawFallback(Long walletId, MoneyRequest req, Throwable ex) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Wallet service unavailable. Please try again."
        );
    }

    @CircuitBreaker(name = "wallet", fallbackMethod = "depositFallback")
    public void walletDeposit(Long walletId, MoneyRequest req) {
        wallet.deposit(walletId, req);
    }

    public void depositFallback(Long walletId, MoneyRequest req, Throwable ex) {
        throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Wallet service unavailable. Please try again."
        );
    }
}
