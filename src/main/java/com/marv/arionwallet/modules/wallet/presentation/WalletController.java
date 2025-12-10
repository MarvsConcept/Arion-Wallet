package com.marv.arionwallet.modules.wallet.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.application.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/fund")
    public ApiResponse<FundWalletResponseDto> fundWallet(Authentication authentication,
                                                         @Valid @RequestBody FundWalletRequestDto request) {
        User currentUser = (User) authentication.getPrincipal();

        FundWalletResponseDto response = walletService.fundWallet(currentUser, request);

        return ApiResponse.ok("Wallet fnded successfully", response);
    }

}
