package com.wallet.youssef.wallet_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions", indexes = {
        @Index(name = "idx_txn_wallet_id", columnList = "wallet_id"),
        @Index(name = "idx_txn_created_at", columnList = "created_at")
})
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one على Wallet
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // DEPOSIT | WITHDRAW

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // لازم تبقى موجبة؛ المنطق ده هنحطه في السيرفس في W-3

    @Column(length = 80)
    private String reference; // اختيارية: رقم طلب/مرجع

    @Column(length = 200)
    private String description; // اختيارية

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- getters/setters ---
    public Long getId() { return id; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
