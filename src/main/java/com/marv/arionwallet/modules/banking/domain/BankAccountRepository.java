package com.marv.arionwallet.modules.banking.domain;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository {
    BankAccount save(BankAccount bankAccount);
    Optional<BankAccount> findByUserIdAndBankCodeAndAccountNumber(UUID userId, String bankCode, String accountNumber);
    Optional<BankAccount> findByIdAndUserId(UUID id, UUID userId);
}
