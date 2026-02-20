package com.marv.arionwallet.modules.admin.presentation;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminActionReasonDto {

    @NotBlank
    private String reason;
}
