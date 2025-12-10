package com.marv.arionwallet.modules.wallet.presentation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FundWalletRequestDto {

    @NotNull
    @Positive
    private Long amountInKobo;

    private String description;
}
