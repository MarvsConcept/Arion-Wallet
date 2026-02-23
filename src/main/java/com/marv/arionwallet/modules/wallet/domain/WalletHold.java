package com.marv.arionwallet.modules.wallet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "wallet_holds",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_hold_tx", columnNames = {"transaction_id"})
        },
        indexes = {
                @Index(name = "idx_wallet_hold_wallet_status", columnList = "wallet_id,status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalletHold {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HoldStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public WalletHold(UUID id,
                      UUID walletId,
                      UUID transactionId,
                      Long amount,
                      String currency,
                      HoldStatus status,
                      Instant createdAt) {

        if (walletId == null) throw new IllegalArgumentException("walletId is required");
        if (transactionId == null) throw new IllegalArgumentException("transactionId is required");
        if (amount == null || amount <= 0) throw new IllegalArgumentException("Hold amount must be positive");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency is required");

        this.id = id != null ? id : UUID.randomUUID();
        this.walletId = walletId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status != null ? status : HoldStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public void consume() {
        if (this.status != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE holds can be consumed");
        }
        this.status = HoldStatus.CONSUMED;
    }

    public void release() {
        if (this.status != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE holds can be released");
        }
        this.status = HoldStatus.RELEASED;
    }

}
