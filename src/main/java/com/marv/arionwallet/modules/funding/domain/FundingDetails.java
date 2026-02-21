package com.marv.arionwallet.modules.funding.domain;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "funding_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_funding_provider_ref",
                columnNames = "provider_reference"
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FundingDetails {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "provider_reference", nullable = false)
    private String providerReference;

    @Column(name = "payment_url")
    private String paymentUrl;


    @Builder
    public FundingDetails(UUID id,
                          Transaction transaction,
                          String providerReference,
                          String paymentUrl,
                          Instant createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.transaction = transaction;
        this.providerReference = providerReference;
        this.paymentUrl = paymentUrl;
    }
}
