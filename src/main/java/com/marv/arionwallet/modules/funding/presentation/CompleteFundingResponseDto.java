package com.marv.arionwallet.modules.funding.presentation;

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
