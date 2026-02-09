package com.marv.arionwallet.modules.kyc.presentation;

import com.marv.arionwallet.modules.kyc.domain.KycStatus;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder
public class KycResponseDto {

    private KycStatus status;
    private KycLevel level;
    private Instant submittedAt;
    private Instant reviewedAt;
    private String rejectionReason;
}
