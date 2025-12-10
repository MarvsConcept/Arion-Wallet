package com.marv.arionwallet.modules.wallet.presentation;

import lombok.*;

@Getter
@AllArgsConstructor
@Builder
public class CompleteFundingResponseDto {

    private String reference;
    private String currency;
    private Long amountInKobo;
    private Long newBalanceInKobo;

}
