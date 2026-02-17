package com.marv.arionwallet.modules.kyc.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.kyc.application.AdminKycService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/kyc")
@RequiredArgsConstructor
public class AdminKycController {

    private final AdminKycService adminKycService;

    @GetMapping("/pending")
    public ApiResponse<Page<KycReviewItemDto>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<KycReviewItemDto> kyPage = adminKycService.listPending(page, size);

        return ApiResponse.ok("Pending KYCs fetched", kyPage);
    }

    @PostMapping("/{userId}/approve")
    public ApiResponse<KycResponseDto> approve(
            @PathVariable UUID userId,
            @Valid @RequestBody KycApproveRequestDto request
            ) {

        KycResponseDto response = adminKycService.approve(userId, request.getLevel());

        return ApiResponse.ok("KYC successfully Approved", response);
    }

    @PostMapping("/{userId}/reject")
    public ApiResponse<KycResponseDto> reject(
            @PathVariable UUID userId,
            @Valid @RequestBody KycRejectRequestDto request
    ) {

        KycResponseDto response = adminKycService.reject(userId, request.getReason());

        return ApiResponse.ok("KYC role rejected", response);
    }

}
