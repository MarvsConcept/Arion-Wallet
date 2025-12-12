package com.marv.arionwallet.modules.ledger.presentation;

import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class LedgerEntryDto {

    private LedgerAccountType accountType;
    private LedgerEntryDirection direction;
    private Long amountInKobo;
    private String currency;
    private UUID walletId;
    private Instant createdAt;
}
