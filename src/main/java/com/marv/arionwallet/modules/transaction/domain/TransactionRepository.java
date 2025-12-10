package com.marv.arionwallet.modules.transaction.domain;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction tx);

    Optional<Transaction> findById(UUID id);

    Optional<Transaction> findByReference(String reference);
}
