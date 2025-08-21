package com.wallet.youssef.wallet_service.web;

import com.wallet.youssef.wallet_service.model.Wallet;
import com.wallet.youssef.wallet_service.model.WalletTransaction;
import com.wallet.youssef.wallet_service.service.WalletService;
import com.wallet.youssef.wallet_service.web.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CreateWalletRequest req) {
        try {
            Wallet w = service.createWallet(req);
            URI location = URI.create("/wallet/balance/" + w.getId());
            return ResponseEntity.created(location).body(toResponse(w));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable Long id, @RequestBody MoneyRequest req) {
        try {
            Wallet w = service.deposit(id, req);
            return ResponseEntity.ok(toResponse(w));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable Long id, @RequestBody MoneyRequest req) {
        try {
            Wallet w = service.withdraw(id, req);
            return ResponseEntity.ok(toResponse(w));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/balance/{id}")
    public ResponseEntity<?> balance(@PathVariable Long id) {
        try {
            Wallet w = service.getWallet(id);
            return ResponseEntity.ok(new BalanceResponse(w.getId(), w.getBalance()));
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<?> transactions(@PathVariable Long id) {
        try {
            List<WalletTransaction> txs = service.transactions(id);
            return ResponseEntity.ok(txs.stream().map(this::toTxResponse).toList());
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private WalletResponse toResponse(Wallet w) {
        return new WalletResponse(w.getId(), w.getOwnerEmail(), w.getBalance(), w.getCreatedAt());
    }

    private TransactionResponse toTxResponse(WalletTransaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType().name(),
                t.getAmount(),
                t.getReference(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
