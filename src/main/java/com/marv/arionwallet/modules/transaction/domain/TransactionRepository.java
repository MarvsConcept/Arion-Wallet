package com.marv.arionwallet.modules.transaction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction tx);

    Optional<Transaction> findById(UUID id);

//    Optional<Transaction> findByReference(String reference);

    Optional<Transaction> findByReferenceAndType(String reference, TransactionType type);

    Page<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, TransactionType type, Pageable pageable);

    Page<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, TransactionStatus status, Pageable pageable);

    Page<Transaction> findByUserIdAndTypeAndStatusOrderByCreatedAtDesc(UUID userId, TransactionType type, TransactionStatus status, Pageable pageable);

    Page<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID userId, Instant start, Instant end, Pageable pageable);

    Optional<Transaction> findByUserIdAndIdempotencyKeyAndType(UUID userId, String idempotencyKey, TransactionType type);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = com.marv.arionwallet.modules.transaction.domain.TransactionType.TRANSFER
      AND t.status = com.marv.arionwallet.modules.transaction.domain.TransactionStatus.SUCCESS
      AND t.createdAt >= :start
      AND t.createdAt < :end
""")
    long sumSuccessfulTransfersForUserBetween(UUID userId, Instant start, Instant end);

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM Transaction t
    WHERE t.user.id = :userId
      AND t.type = com.marv.arionwallet.modules.transaction.domain.TransactionType.WITHDRAWAL
      AND t.status IN (
            com.marv.arionwallet.modules.transaction.domain.TransactionStatus.PENDING
            com.marv.arionwallet.modules.transaction.domain.TransactionStatus.SUCCESS
      AND t.createdAt >= :start
      AND t.createdAt < :end
""")
    long sumNonFailedWithdrawalsForUserBetween(UUID userId, Instant start, Instant end);


    @Modifying
    @Query("""
  UPDATE Transaction t
  SET t.status = com.marv.arionwallet.modules.transaction.domain.TransactionStatus.FAILED
  WHERE t.type = com.marv.arionwallet.modules.transaction.domain.TransactionType.WITHDRAWAL
    AND t.status = com.marv.arionwallet.modules.transaction.domain.TransactionStatus.PENDING
    AND t.createdAt < :cutoff
""")
    int findStalePendingWithdrawals(Instant cutoff);
}






