package com.marv.arionwallet.modules.kyc.application;

import com.marv.arionwallet.modules.kyc.domain.KycProfile;
import com.marv.arionwallet.modules.kyc.domain.KycProfileRepository;
import com.marv.arionwallet.modules.kyc.domain.KycStatus;
import com.marv.arionwallet.modules.kyc.presentation.KycResponseDto;
import com.marv.arionwallet.modules.kyc.presentation.KycReviewItemDto;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminKycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<KycReviewItemDto> listPending(int page, int size) {

        // Create a pageable
        Pageable pageable = PageRequest.of(page, size);

        Page<KycProfile> kycProfilesPage;

        kycProfilesPage = kycProfileRepository.findByStatusOrderBySubmittedAtAsc(KycStatus.PENDING, pageable);

        return kycProfilesPage.map(ky ->
                KycReviewItemDto.builder()
                        .userId(ky.getUser().getId())
                        .fullName(ky.getFullName())
                        .status(ky.getStatus())
//                        .requestedLevel(ky.getLevel())
                        .submittedAt(ky.getSubmittedAt())
                        .build()
                );
    }

    @Transactional
    public KycResponseDto approve(UUID userId, KycLevel level) {

        // Load KycProfile by userId
        KycProfile profile = kycProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("KYC profile not found"));

        // ensure status is PENDING
        if (profile.getStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("Only pending KYC can be approved");
        }

        // approve
        profile.approve(level);
        kycProfileRepository.save(profile);

        // Update user KYC level too
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.upgradeKyc(level);
        userRepository.save(user);

        return toResponse(profile);
    }

    @Transactional
    public KycResponseDto reject(UUID userId, String reasom) {

        // Load KycProfile by userId
        KycProfile profile = kycProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("KYC profile not found"));

        // ensure status is PENDING
        if (profile.getStatus() != KycStatus.PENDING) {
            throw new IllegalStateException("Only pending KYC can be approved");
        }

        // reject
        profile.reject(reasom);
        kycProfileRepository.save(profile);

        // user stays NONE on rejection
        return toResponse(profile);
    }

    private KycResponseDto toResponse(KycProfile profile) {
        return KycResponseDto.builder()
                .status(profile.getStatus())
                .level(profile.getLevel())
                .submittedAt(profile.getSubmittedAt())
                .reviewedAt(profile.getReviewedAt())
                .rejectionReason(profile.getRejectionReason())
                .build();
    }
}
