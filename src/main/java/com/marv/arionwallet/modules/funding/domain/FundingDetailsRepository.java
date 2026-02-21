package com.marv.arionwallet.modules.funding.domain;

import com.marv.arionwallet.modules.transaction.domain.Transaction;

import java.util.Optional;

public interface FundingDetailsRepository {
    FundingDetails save(FundingDetails details);
    Optional<FundingDetails> findByProviderReference(String providerReference);
    Optional<FundingDetails> findByTransaction(Transaction transaction);
    Optional<FundingDetails> findByTransactionReference(String reference);
}
