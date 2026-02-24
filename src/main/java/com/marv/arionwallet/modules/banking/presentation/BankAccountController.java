package com.marv.arionwallet.modules.banking.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.banking.application.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PatchMapping("/{id}/default")
    public ApiResponse<Void> setDefault(Authentication authentication,
                                        @PathVariable UUID id) {

        User currentUser = (User) authentication.getPrincipal();
        bankAccountService.setDefaultBankAccount(currentUser, id);
        return ApiResponse.ok("Default bank account updated", null);
    }

    @GetMapping
    public ApiResponse<List<BankAccountResponseDto>> list(Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        List<BankAccountResponseDto> response = bankAccountService.listMyAccounts(currentUser);

        return ApiResponse.ok("Bank account fetched", response );
    }

    @GetMapping("/default")
    public ApiResponse<BankAccountResponseDto> getDefault(Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        BankAccountResponseDto response = bankAccountService.getDefaultAccount(currentUser);

        return ApiResponse.ok("Default bank account fetched", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication authentication) {

        User currentUser = (User) authentication.getPrincipal();

        bankAccountService.deleteAccount(currentUser, id);
        return ApiResponse.ok("Bank account deleted",null);
    }
}
