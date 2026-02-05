package com.marv.arionwallet.modules.withdrawal.presentation;

import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class WithdrawalHistoryItemDto {

    private String reference;
    private TransactionStatus status;
    private long amountInKobo;
    private String currency;

    private String bankCode;
    private String accountName;
    private String accountNumberMasked;

    private Instant createdAt;
}
