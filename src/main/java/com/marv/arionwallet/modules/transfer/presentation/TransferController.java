package com.marv.arionwallet.modules.transfer.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.transfer.application.TransferService;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ApiResponse<TransferResponseDto> transfer(
            Authentication authentication,
            @RequestHeader(value = "Idempotency-key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        TransferResponseDto response = transferService.transfer(currentUser, request, idempotencyKey);

        return ApiResponse.ok("Transfer done successfully", response);

    }

}
