package com.marv.arionwallet.modules.banking.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankAccountRequestDto {

    @NotBlank
    private String bankCode;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "Account number must be 10 digits")
    private String accountNumber;
}
