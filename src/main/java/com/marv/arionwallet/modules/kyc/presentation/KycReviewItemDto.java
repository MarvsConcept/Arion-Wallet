package com.marv.arionwallet.modules.kyc.presentation;

import com.marv.arionwallet.modules.kyc.domain.KycStatus;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class KycReviewItemDto {

    private UUID userId;
    private String fullName;
    private KycStatus status;
    private KycLevel level;
    private KycLevel requestedLevel;
    private Instant submittedAt;
}
