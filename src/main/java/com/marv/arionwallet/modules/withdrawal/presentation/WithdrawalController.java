package com.marv.arionwallet.modules.withdrawal.presentation;


import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.transfer.application.TransferService;
import com.marv.arionwallet.modules.transfer.presentation.TransferRequestDto;
import com.marv.arionwallet.modules.transfer.presentation.TransferResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.withdrawal.application.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdrawals")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ApiResponse<WithdrawalResponseDto> withdraw(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-key", required = false) String idempotencyKey,
            @Valid @RequestBody WithdrawalRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        WithdrawalResponseDto response = withdrawalService.requestWithdrawal(currentUser, request, idempotencyKey);

        return ApiResponse.ok("Withdrawal initiated successfully", response);
    }

    @PostMapping("/{reference}/complete")
    public ApiResponse<WithdrawalResponseDto> completeWithdrawal(
            @PathVariable String reference) {

        WithdrawalResponseDto response = withdrawalService.completeWithdrawal(reference);

        return ApiResponse.ok("Withdrawal processed", response);
    }

    @GetMapping("/{reference}")
    public ApiResponse<WithdrawalDetailsResponseDto> getWithdrawal(
            Authentication authentication,
            @PathVariable String reference
    ) {
        User currentUser = (User) authentication.getPrincipal();
        WithdrawalDetailsResponseDto dto = withdrawalService.getWithdrawal(currentUser, reference);

        return ApiResponse.ok("Withdrawal fetched", dto);
    }


    @GetMapping
    public ApiResponse<Page<WithdrawalHistoryItemDto>> listWithdrawals(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        User currentUser = (User) authentication.getPrincipal();
        Page<WithdrawalHistoryItemDto> result = withdrawalService
                .listWithdrawals(currentUser, PageRequest.of(page, size));

        return ApiResponse.ok("Withdrawals fetched", result);
    }
}
