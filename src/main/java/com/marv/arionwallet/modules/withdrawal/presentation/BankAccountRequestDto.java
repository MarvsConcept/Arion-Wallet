package com.marv.arionwallet.modules.withdrawal.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequestDto {

    @NotBlank
    private String bankCode;

    @NotBlank
    private String accountNumber;
}
