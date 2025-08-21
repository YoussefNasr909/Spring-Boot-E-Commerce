package com.wallet.youssef.wallet_service.repository;

import com.wallet.youssef.wallet_service.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWallet_IdOrderByCreatedAtDesc(Long walletId);
}
