package com.marv.arionwallet.modules.transaction.infrastructure;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID>, TransactionRepository {

    @Override
    Optional<Transaction> findByReference(String reference);

}
