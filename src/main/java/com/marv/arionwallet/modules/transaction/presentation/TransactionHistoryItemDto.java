package com.marv.arionwallet.modules.transaction.presentation;

import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class TransactionHistoryItemDto {

    private String reference;
    private TransactionType type;
    private TransactionStatus status;
    private Long amountInKobo;
    private String currency;
    private String description;
    private Instant createdAt;
}
