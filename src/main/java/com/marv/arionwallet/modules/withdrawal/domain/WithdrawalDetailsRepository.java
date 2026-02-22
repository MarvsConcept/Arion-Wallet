package com.marv.arionwallet.modules.withdrawal.domain;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface WithdrawalDetailsRepository {
    WithdrawalDetails save(WithdrawalDetails details);

    Optional<WithdrawalDetails> findByTransaction(Transaction transaction);

    Optional<WithdrawalDetails> findByProviderReference(String providerReference);

    Optional<WithdrawalDetails> findByTransaction_User_IdAndTransaction_Reference(UUID userId, String reference);

    Page<WithdrawalDetails> findByTransaction_User_IdOrderByTransaction_CreatedAtDesc(UUID userId, Pageable pageable);

}
