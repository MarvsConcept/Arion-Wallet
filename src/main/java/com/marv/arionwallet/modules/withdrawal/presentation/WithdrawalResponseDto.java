package com.marv.arionwallet.modules.withdrawal.presentation;

import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class WithdrawalResponseDto {

    private String reference;
    private TransactionStatus status;
    private long amountInKobo;
    private String currency;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private Instant createdAt;

}
