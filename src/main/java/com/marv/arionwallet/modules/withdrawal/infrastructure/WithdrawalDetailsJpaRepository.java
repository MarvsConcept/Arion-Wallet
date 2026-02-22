package com.marv.arionwallet.modules.withdrawal.infrastructure;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetails;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetailsRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawalDetailsJpaRepository
        extends JpaRepository<WithdrawalDetails, UUID>, WithdrawalDetailsRepository {

    @Override
    Optional<WithdrawalDetails> findByTransaction(Transaction transaction);

    @Override
    Optional<WithdrawalDetails> findByProviderReference(String providerReference);
}
