package com.marv.arionwallet.modules.wallet.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.policy.application.AccessPolicyService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import com.marv.arionwallet.modules.funding.presentation.CompleteFundingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccessPolicyService accessPolicyService;



    @Transactional
    public CompleteFundingResponseDto completeFunding(String reference) {

        // Find transaction by reference
        Transaction transaction = transactionRepository.findByReferenceAndType(reference, TransactionType.FUNDING)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reference"));

        // Guard against double-processing (Idempotency-lite)
        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            throw new IllegalStateException("Transaction already completed");
        }
        if (transaction.getStatus() == TransactionStatus.FAILED) {
            throw new IllegalStateException("Transaction already failed");
        }

        // Load the wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(
                transaction.getUser().getId(),
                transaction.getCurrency()
        )
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        // Mark Transaction as Success
        transaction.markSuccess();
        transaction = transactionRepository.save(transaction);

        // Credit Wallet
        wallet.credit(transaction.getAmount());
        walletRepository.save(wallet);

        // Create External Ledger Entry
        LedgerEntry externalEntry = LedgerEntry.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .wallet(null)
                .accountType(LedgerAccountType.EXTERNAL_FUNDING)
                .direction(LedgerEntryDirection.DEBIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        // Create Wallet Ledger Entry
        LedgerEntry walletEntry = LedgerEntry.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .wallet(wallet)
                .accountType(LedgerAccountType.USER_WALLET)
                .direction(LedgerEntryDirection.CREDIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        // Save Ledger
        ledgerEntryRepository.save(externalEntry);
        ledgerEntryRepository.save(walletEntry);

        // Build response
        return CompleteFundingResponseDto.builder()
                .reference(reference)
                .currency(wallet.getCurrency())
                .amountInKobo(transaction.getAmount())
                .newBalanceInKobo(wallet.getBalance())
                .build();

    }
}
