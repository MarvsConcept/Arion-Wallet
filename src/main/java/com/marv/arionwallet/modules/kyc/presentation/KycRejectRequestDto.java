package com.marv.arionwallet.modules.kyc.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycRejectRequestDto {
    @NotBlank
    private String reason;
}
