package com.marv.arionwallet.modules.kyc.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.kyc.application.KycService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    public ApiResponse<KycResponseDto> submit(
            Authentication authentication,
            @Valid @RequestBody KycSubmitRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        KycResponseDto response = kycService.submit(currentUser, request);
        return ApiResponse.ok("Kyc details submitted successfully", response);
    }

    @GetMapping("/me")
    public ApiResponse<KycResponseDto> me(
            Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        KycResponseDto response = kycService.getMyKyc(currentUser);

        return ApiResponse.ok("Kyc details fetched successfully", response);
    }

}
