package com.marv.arionwallet.modules.banking.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.banking.application.BankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banks")
@RequiredArgsConstructor
public class BankController {

    private final BankingService bankingService;

    @GetMapping
    public ApiResponse<List<BankDto>> listBanks() {
        return ApiResponse.ok("Banks fetched", bankingService.listBanks());
    }
}
