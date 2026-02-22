package com.marv.arionwallet.modules.payout.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayoutWebhookRequestDto {
    @NotBlank
    private String providerReference;

    @NotNull
    private PayoutWebhookStatus status;
}