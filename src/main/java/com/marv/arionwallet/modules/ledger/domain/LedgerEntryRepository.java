package com.marv.arionwallet.modules.ledger.domain;

import java.util.List;

public interface LedgerEntryRepository {

    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByTransactionReferenceOrderByCreatedAtAsc(String reference);
}
