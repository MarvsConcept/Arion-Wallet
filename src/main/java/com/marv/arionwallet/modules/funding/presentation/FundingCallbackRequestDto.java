package com.marv.arionwallet.modules.funding.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FundingCallbackRequestDto {

    @NotBlank
    private String reference;
}
