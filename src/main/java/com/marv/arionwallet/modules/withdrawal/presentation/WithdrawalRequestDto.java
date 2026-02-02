package com.marv.arionwallet.modules.withdrawal.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class WithdrawalRequestDto {

    @NotNull
    @Min(1)
    private long amountInKobo;

    @NotBlank
    private String currency;

    @NotNull
    private UUID bankAccountId;
}
