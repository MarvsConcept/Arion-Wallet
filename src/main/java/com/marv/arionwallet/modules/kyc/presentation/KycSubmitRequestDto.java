package com.marv.arionwallet.modules.kyc.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class KycSubmitRequestDto {

    @NotBlank
    private String fullName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotBlank
    private String address;

    @NotBlank
    private String idType;

    @NotBlank
    private String idNumber;
}
