package com.marv.arionwallet.modules.banking.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository {

    BankAccount save(BankAccount bankAccount);
    Optional<BankAccount> findByUserIdAndBankCodeAndAccountNumber(UUID userId, String bankCode, String accountNumber);
    Optional<BankAccount> findByIdAndUserId(UUID id, UUID userId);
    List<BankAccount> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("update BankAccount b set b.isDefault = false where b.user.id = :userId and b.isDefault = true")
    void clearDefaultForUser(@Param("userId") UUID userId);

    Optional<BankAccount> findByUserIdAndIsDefaultTrue(UUID userId);

    Optional<BankAccount> findTopByUserIdOrderByCreatedAtDesc(UUID userId);

    void delete(BankAccount bankAccount);

}
