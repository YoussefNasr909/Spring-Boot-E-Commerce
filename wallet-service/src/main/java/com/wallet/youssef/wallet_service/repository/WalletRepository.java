package com.wallet.youssef.wallet_service.repository;

import com.wallet.youssef.wallet_service.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByOwnerEmail(String ownerEmail);
    boolean existsByOwnerEmail(String ownerEmail);
}
