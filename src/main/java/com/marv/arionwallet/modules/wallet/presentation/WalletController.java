package com.marv.arionwallet.modules.wallet.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.transaction.application.TransactionService;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.transaction.presentation.TransactionHistoryItemDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.application.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @PostMapping("/fund")
    public ApiResponse<InitiateFundingResponseDto> initialFunding(
            Authentication authentication,
            @Valid @RequestBody FundWalletRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        InitiateFundingResponseDto response = walletService.initiateFunding(currentUser, request);

        return ApiResponse.ok("Funding initiated successfully", response);
    }

    @PostMapping("/fund/callback")
    public ApiResponse<CompleteFundingResponseDto> completeFunding(
                        @Valid @RequestBody FundingCallbackRequestDto request) {

        CompleteFundingResponseDto response = walletService.completeFunding(request.getReference());

        return ApiResponse.ok("Funding completed successfully", response);
    }

    @GetMapping("/transactions")
    public ApiResponse<Page<TransactionHistoryItemDto>> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionType type
            ) {

        // Get current user
        User currentUser = (User) authentication.getPrincipal();

        Page<TransactionHistoryItemDto> txPage = transactionService.getUserTransactions(currentUser, type, page, size);

        return ApiResponse.ok("Transactions fetched successfully", txPage);
    }


}
