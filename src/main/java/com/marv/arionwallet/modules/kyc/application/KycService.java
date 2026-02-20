package com.marv.arionwallet.modules.kyc.application;

import com.marv.arionwallet.modules.kyc.domain.KycProfile;
import com.marv.arionwallet.modules.kyc.domain.KycProfileRepository;
import com.marv.arionwallet.modules.kyc.domain.KycStatus;
import com.marv.arionwallet.modules.kyc.presentation.KycResponseDto;
import com.marv.arionwallet.modules.kyc.presentation.KycSubmitRequestDto;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycProfileRepository;

    @Transactional
    public KycResponseDto submit(User currentUser,
                                 KycSubmitRequestDto request) {

        KycProfile profile = kycProfileRepository.findByUserId(currentUser.getId())
                .orElseGet(()-> KycProfile.builder()
                        .user(currentUser)
//                        .status(KycStatus.PENDING)
//                        .level(KycLevel.NONE)
                        .build()
                );

        profile.submitWithDetails(
                request.getFullName(),
                request.getDateOfBirth(),
                request.getAddress(),
                request.getIdType(),
                request.getIdNumber(),
                request.getRequestedLevel()
        );

        profile = kycProfileRepository.save(profile);

        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public KycResponseDto getMyKyc(User currentUser) {

        return kycProfileRepository.findByUserId(currentUser.getId())
                .map(this::toResponse)
                .orElseGet(() -> KycResponseDto.builder()
                        .status(KycStatus.NOT_SUBMITTED)
                        .level(KycLevel.NONE)
                        .submittedAt(null)
                        .reviewedAt(null)
                        .rejectionReason(null)
                        .build());
    }

    private KycResponseDto toResponse(KycProfile profile) {
        return KycResponseDto.builder()
                .status(profile.getStatus())
                .level(profile.getLevel())
                .requestedLevel(profile.getRequestedLevel())
                .submittedAt(profile.getSubmittedAt())
                .reviewedAt(profile.getReviewedAt())
                .rejectionReason(profile.getRejectionReason())
                .build();
    }


}


