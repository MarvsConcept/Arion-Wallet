package com.marv.arionwallet.modules.payments.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentWebhookRequestDto {

    @NotBlank
    private String providerReference;

    @NotNull
    private PaymentWebhookStatus status;

}
