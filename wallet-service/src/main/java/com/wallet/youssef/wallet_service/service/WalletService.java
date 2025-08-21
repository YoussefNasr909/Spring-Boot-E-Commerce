package com.wallet.youssef.wallet_service.service;

import com.wallet.youssef.wallet_service.model.TransactionType;
import com.wallet.youssef.wallet_service.model.Wallet;
import com.wallet.youssef.wallet_service.model.WalletTransaction;
import com.wallet.youssef.wallet_service.repository.WalletRepository;
import com.wallet.youssef.wallet_service.repository.WalletTransactionRepository;
import com.wallet.youssef.wallet_service.web.dto.CreateWalletRequest;
import com.wallet.youssef.wallet_service.web.dto.MoneyRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WalletService {

    private final WalletRepository wallets;
    private final WalletTransactionRepository txRepo;

    public WalletService(WalletRepository wallets, WalletTransactionRepository txRepo) {
        this.wallets = wallets;
        this.txRepo = txRepo;
    }

    @Transactional
    public Wallet createWallet(CreateWalletRequest req) {
        if (req == null || req.ownerEmail == null || req.ownerEmail.isBlank()) {
            throw new IllegalArgumentException("ownerEmail is required");
        }
        if (wallets.existsByOwnerEmail(req.ownerEmail)) {
            throw new IllegalArgumentException("Wallet already exists for this email");
        }
        Wallet w = new Wallet();
        w.setOwnerEmail(req.ownerEmail.trim());
        // balance & createdAt بيتضبطوا في @PrePersist
        return wallets.save(w);
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(Long walletId) {
        return wallets.findById(walletId)
                .orElseThrow(() -> new NoSuchElementException("Wallet not found"));
    }

    @Transactional
    public Wallet deposit(Long walletId, MoneyRequest req) {
        Wallet w = getWallet(walletId);
        BigDecimal amount = validateAmount(req);

        // زوّد الرصيد
        w.setBalance(w.getBalance().add(amount));

        // سجّل العملية
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(w);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setReference(req.reference);
        tx.setDescription(req.description);
        txRepo.save(tx);

        return w;
    }

    @Transactional
    public Wallet withdraw(Long walletId, MoneyRequest req) {
        Wallet w = getWallet(walletId);
        BigDecimal amount = validateAmount(req);

        if (w.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // اخصم الرصيد
        w.setBalance(w.getBalance().subtract(amount));

        // سجّل العملية
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(w);
        tx.setType(TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setReference(req.reference);
        tx.setDescription(req.description);
        txRepo.save(tx);

        return w;
    }

    @Transactional(readOnly = true)
    public List<WalletTransaction> transactions(Long walletId) {
        // مش هنرجّع الـ Wallet نفسه علشان نتفادى Lazy؛ {wallet_id} بس
        // هنجيب بالـ walletId مباشرة
        if (!wallets.existsById(walletId)) {
            throw new NoSuchElementException("Wallet not found");
        }
        return txRepo.findByWallet_IdOrderByCreatedAtDesc(walletId);
    }

    private BigDecimal validateAmount(MoneyRequest req) {
        if (req == null || req.amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (req.amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        return req.amount;
    }
}
