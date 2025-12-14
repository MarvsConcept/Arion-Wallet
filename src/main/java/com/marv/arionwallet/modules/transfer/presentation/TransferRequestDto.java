package com.marv.arionwallet.modules.transfer.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDto {

    @NotBlank
    private String recipientAccountNumber;

    @NotNull
    @Min(1)
    private Long amountInKobo;

    @NotBlank
    private String currency;

    private String narration;
}
