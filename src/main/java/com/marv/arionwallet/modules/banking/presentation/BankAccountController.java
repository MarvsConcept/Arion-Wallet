package com.marv.arionwallet.modules.banking.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.banking.application.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ApiResponse<BankAccountResponseDto> add(
            Authentication authentication,
            @Valid @RequestBody BankAccountRequestDto request) {

        User currentUser = (User) authentication.getPrincipal();

        BankAccountResponseDto response = bankAccountService.addBankAccount(currentUser, request);

        return ApiResponse.ok("Bank account saved", response);
    }

}
