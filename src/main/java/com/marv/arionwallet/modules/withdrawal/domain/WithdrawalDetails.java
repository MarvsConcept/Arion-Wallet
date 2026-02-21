package com.marv.arionwallet.modules.withdrawal.domain;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_details")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalDetails {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "bank_code", nullable = false)
    private String bankCode;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "provider_reference")
    private String providerReference;


    @Builder
    public WithdrawalDetails(UUID id,
                             Transaction transaction,
                             String bankCode,
                             String accountNumber,
                             String accountName,
                             String providerReference) {
        Instant now = Instant.now();
        this.id = id != null ? id : UUID.randomUUID();
        this.transaction = transaction;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.providerReference = providerReference;
    }

    public void setProviderReferenceIfAbsent(String ref) {
        if (this.providerReference == null && ref != null && !ref.isBlank()) {
            this.providerReference = ref;
        }
    }
}
