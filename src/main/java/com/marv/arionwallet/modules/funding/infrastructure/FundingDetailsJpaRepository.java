package com.marv.arionwallet.modules.funding.infrastructure;

import com.marv.arionwallet.modules.funding.domain.FundingDetails;
import com.marv.arionwallet.modules.funding.domain.FundingDetailsRepository;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundingDetailsJpaRepository
        extends JpaRepository<FundingDetails, UUID>, FundingDetailsRepository {

    Optional<FundingDetails> findByProviderReference(String providerReference);
    Optional<FundingDetails> findByTransaction(Transaction transaction);
    Optional<FundingDetails> findByTransaction_Reference(String reference);

    @Override
    default Optional<FundingDetails> findByTransactionReference(String reference) {
        return findByTransaction_Reference(reference);
    }
}
