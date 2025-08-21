package com.wallet.youssef.wallet_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets", indexes = {
        @Index(name = "idx_wallet_owner_email", columnList = "owner_email")
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // مناسب لـ MySQL
    private Long id;

    @Column(name = "owner_email", nullable = false, length = 180)
    private String ownerEmail;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // --- lifecycle ---
    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
    }

    // --- getters/setters ---
    public Long getId() { return id; }

    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
