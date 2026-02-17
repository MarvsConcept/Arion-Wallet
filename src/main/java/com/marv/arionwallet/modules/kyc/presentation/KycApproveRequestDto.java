package com.marv.arionwallet.modules.kyc.presentation;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KycApproveRequestDto {
    @NotNull
    private KycLevel level;
}
