package com.marv.arionwallet.modules.banking.infrastructure;

import com.marv.arionwallet.modules.banking.domain.BankAccount;
import com.marv.arionwallet.modules.banking.domain.BankAccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankAccountJpaRepository
        extends JpaRepository<BankAccount, UUID>, BankAccountRepository {
}
