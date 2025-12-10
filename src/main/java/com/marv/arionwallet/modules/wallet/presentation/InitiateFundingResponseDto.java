package com.marv.arionwallet.modules.wallet.presentation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class InitiateFundingResponseDto {

    private String reference;
    private String currency;
    private Long amountInKobo;

}
