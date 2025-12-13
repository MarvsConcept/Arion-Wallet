package com.marv.arionwallet.modules.ledger.presentation;

import com.marv.arionwallet.core.dto.ApiResponse;
import com.marv.arionwallet.modules.ledger.application.LedgerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions/{reference}")
    public ApiResponse<List<LedgerEntryDto>> getLedger(@PathVariable String reference) {

        List<LedgerEntryDto> entries = ledgerService.getEntriesForTransaction(reference);

        return ApiResponse.ok("Ledger returned successfully", entries);
    }
}
