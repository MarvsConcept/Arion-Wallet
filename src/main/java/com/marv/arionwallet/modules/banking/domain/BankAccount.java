package com.marv.arionwallet.modules.banking.domain;

import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bank_account",
        uniqueConstraints = @UniqueConstraint(
        name = "uk_user_bank_account",
        columnNames = {"user_id", "bank_code", "account_number"}
)
)
@Getter
@NoArgsConstructor
public class BankAccount {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "account_number",nullable = false)
    private String accountNumber;

    @Column(name = "account_name",nullable = false)
    private String accountName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Builder
    public BankAccount(UUID id,
                       User user,
                       String bankCode,
                       String accountNumber,
                       String accountName,
                       Instant createdAt) {
        Instant now = Instant.now();

        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.createdAt = createdAt != null ? createdAt : now;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
