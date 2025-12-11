package com.marv.arionwallet.modules.transaction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction tx);

    Optional<Transaction> findById(UUID id);

    Optional<Transaction> findByReference(String reference);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TransactionStatus status, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndStatusOrderByCreatedAtDesc(UUID userId, TransactionType type, TransactionStatus status, Pageable pageable);

}
