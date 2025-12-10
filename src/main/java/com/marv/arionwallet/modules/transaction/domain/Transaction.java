package com.marv.arionwallet.modules.transaction.domain;

import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.domain.WalletStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false)
    private Instant updatedAt;

    @Builder

    public Transaction(UUID id,
                       User user,
                       TransactionType type,
                       TransactionStatus status,
                       Long amount,
                       String currency,
                       String reference,
                       String description,
                       Instant createdAt,
                       Instant updatedAt) {
        Instant now = Instant.now();
        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.type = type;
        this.status = status != null ? status : TransactionStatus.PENDING;
        this.amount = amount;
        this.currency = currency;
        this.reference = reference;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : now;
        this.updatedAt = updatedAt != null ? updatedAt : now;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
