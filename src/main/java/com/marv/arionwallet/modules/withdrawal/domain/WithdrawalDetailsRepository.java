package com.marv.arionwallet.modules.withdrawal.domain;

import com.marv.arionwallet.modules.transaction.domain.Transaction;

import java.util.Optional;

public interface WithdrawalDetailsRepository {
    WithdrawalDetails save(WithdrawalDetails details);

    Optional<WithdrawalDetails> findByTransactionReference(String reference);

    Optional<WithdrawalDetails> findByTransaction(Transaction transaction);
}
