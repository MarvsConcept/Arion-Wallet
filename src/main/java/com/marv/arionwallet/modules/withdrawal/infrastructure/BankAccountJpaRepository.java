package com.marv.arionwallet.modules.withdrawal.infrastructure;

import com.marv.arionwallet.modules.withdrawal.domain.BankAccount;
import com.marv.arionwallet.modules.withdrawal.domain.BankAccountRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BankAccountJpaRepository
        extends JpaRepository<BankAccount, UUID>, BankAccountRepository {
}
