package com.marv.arionwallet.modules.withdrawal.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {

    @PostMapping
    public ApiResponse<> bankaccount
}
