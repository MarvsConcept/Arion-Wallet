package com.marv.arionwallet.modules.transfer.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Builder
public class TransferResponseDto {

    private String reference;
    private Long amountInKobo;
    private String currency;
    private String senderFullName;
    private String senderAccountNumber;
    private String recipientFullName;
    private String recipientAccountNumber;
    private Long senderNewBalance;
    private Long recipientNewBalance;
    private String narration;
    private Instant createdAt;
}
