package com.marv.arionwallet.modules.withdrawal.presentation;


import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.transfer.application.TransferService;
import com.marv.arionwallet.modules.transfer.presentation.TransferRequestDto;
import com.marv.arionwallet.modules.transfer.presentation.TransferResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.withdrawal.application.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdraw")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;

    @PostMapping
    public ApiResponse<WithdrawalResponseDto> withdraw(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-key") String idempotencyKey,
            @Valid @RequestBody WithdrawalRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        WithdrawalResponseDto response = withdrawalService.requestWithdrawal(currentUser, request, idempotencyKey);

        return ApiResponse.ok("Withdrawal initiated successfully", response);
    }
}
