package com.marv.arionwallet.modules.banking.presentation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class BankAccountResponseDto {

    private UUID id;
    private String bankCode;
    private String accountNumber;
    private String accountName;
    private Instant createdAt;
}
