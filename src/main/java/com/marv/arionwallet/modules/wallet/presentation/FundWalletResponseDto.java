package com.marv.arionwallet.modules.wallet.presentation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FundWalletResponseDto {

    private String reference;
    private String currency;
    private Long amountInKobo;
    private Long newBalanceInKobo;
}
