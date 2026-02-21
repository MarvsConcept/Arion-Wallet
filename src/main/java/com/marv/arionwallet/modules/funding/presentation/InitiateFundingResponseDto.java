package com.marv.arionwallet.modules.funding.presentation;

import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class InitiateFundingResponseDto {

    private String reference;
    private String currency;
    private TransactionStatus status;
    private Long amountInKobo;
    private String paymentUrl;
    private Instant createdAt;

}
