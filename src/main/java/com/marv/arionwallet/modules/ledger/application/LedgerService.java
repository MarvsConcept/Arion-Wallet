package com.marv.arionwallet.modules.ledger.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.ledger.presentation.LedgerEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public List<LedgerEntryDto> getEntriesForTransaction(String reference) {

        // Validate reference
        if (reference == null || reference.isBlank()){
            throw new IllegalArgumentException("Provide a valid reference");
        }

        // Call the repo
        List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionReferenceOrderByCreatedAtAsc(reference);

        if (entries.isEmpty()) {
            throw new IllegalArgumentException("No ledger entries found for reference");
        }

//        List<LedgerEntryDto> dtos = new ArrayList<>();

        //List
//        for (LedgerEntry e : entries) {
//            UUID walletId = (e.getWallet() != null) ? e.getWallet().getId() : null;
//
//            LedgerEntryDto dto = LedgerEntryDto.builder()
//                    .accountType(e.getAccountType())
//                    .direction(e.getDirection())
//                    .amountInKobo(e.getAmount())
//                    .currency(e.getCurrency())
//                    .walletId(walletId)
//                    .createdAt(e.getCreatedAt())
//                    .build();
//            dtos.add(dto);
//        }
//        return dtos;
//    }


        // Stream - Shorter
        return entries.stream()
                .map(e -> LedgerEntryDto.builder()
                        .accountType(e.getAccountType())
                        .direction(e.getDirection())
                        .amountInKobo(e.getAmount())
                        .currency(e.getCurrency())
                        .walletId(e.getWallet() != null ? e.getWallet().getId() : null)
                        .createdAt(e.getCreatedAt())
                        .build())
                .toList();
    }

}

