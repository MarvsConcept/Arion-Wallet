package com.marv.arionwallet.modules.funding.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.funding.application.FundingService;
import com.marv.arionwallet.modules.transaction.application.TransactionService;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.application.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/funding")
@RequiredArgsConstructor
public class FundingController {

    private final FundingService fundingService;
    private final TransactionService transactionService;

    @PostMapping("/fund")
    public ApiResponse<InitiateFundingResponseDto> initialFunding(
            Authentication authentication,
            @Valid @RequestBody FundWalletRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        InitiateFundingResponseDto response = fundingService.initiateFunding(currentUser, request);

        return ApiResponse.ok("Funding initiated successfully", response);
    }

//    @PostMapping("/fund/callback")
//    public ApiResponse<CompleteFundingResponseDto> completeFunding(
//                        @Valid @RequestBody FundingCallbackRequestDto request) {
//
//        CompleteFundingResponseDto response = walletService.completeFunding(request.getReference());
//
//        return ApiResponse.ok("Funding completed successfully", response);
//    }

    @PostMapping("/fund/{reference}")
    public ApiResponse<CompleteFundingResponseDto> completeFunding(
            @PathVariable String reference) {

        CompleteFundingResponseDto response = fundingService.completeFunding(reference);

        return ApiResponse.ok("Funding completed successfully", response);
    }
}
