package com.marv.arionwallet.modules.wallet.domain;

import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // For now we'll stick with a simple String; later we can make a Currency enum.
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    // Store money as smallest unit (kobo), never as double.
    @Column(name = "balance", nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WalletStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    public Wallet(UUID id,
                  User user,
                  String currency,
                  Long balance,
                  WalletStatus status,
                  Instant createdAt,
                  Instant updatedAt) {
        Instant now = Instant.now();

        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.currency = currency != null ? currency : "NGN";
        this.balance = balance != null ? balance : 0L;
        this.status = status != null ? status : WalletStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : now;
        this.updatedAt = updatedAt != null ? updatedAt : now;
    }

    // Domain methods

    public void credit(long amountInKobo) {
        if (amountInKobo <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance += amountInKobo;
        this.updatedAt = Instant.now();
    }

    public void debit(long amountInKobo) {
        if (amountInKobo <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (this.balance < amountInKobo)  {
            throw new IllegalStateException("Insufficient Balance");
        }
        this.balance -= amountInKobo;
        this.updatedAt = Instant.now();
    }

    public void freeze() {
        this.status = WalletStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    public void unfreeze() {
        this.status = WalletStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

}
