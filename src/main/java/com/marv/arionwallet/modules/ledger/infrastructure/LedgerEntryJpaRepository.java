package com.marv.arionwallet.modules.ledger.infrastructure;

import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntry, UUID>, LedgerEntryRepository {



}
