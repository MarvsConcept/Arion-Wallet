package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.risk.application.FraudService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.transfer.presentation.TransferRequestDto;
import com.marv.arionwallet.modules.transfer.presentation.TransferResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import com.marv.arionwallet.modules.withdrawal.domain.BankAccount;
import com.marv.arionwallet.modules.withdrawal.domain.BankAccountRepository;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetails;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetailsRepository;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalRequestDto;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final WithdrawalDetailsRepository withdrawalDetailsRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;
    private final FraudService fraudService;

    @Transactional
    public WithdrawalResponseDto requestWithdrawal(User user,
                                                   WithdrawalRequestDto request,
                                                   String idempotencyKey) {

        // Validate Currency
        if (!request.getCurrency().trim().equalsIgnoreCase("NGN")) {
            throw new IllegalArgumentException("Only NGN is supported");
        }

        // Validate amount > 0
        if (request.getAmountInKobo() <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        // Check idempotency
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existingTx = transactionRepository.findByUserIdAndIdempotencyKeyAndType(user.getId(), idempotencyKey, TransactionType.WITHDRAWAL);

            if (existingTx.isPresent()) {
                Transaction tx = existingTx.get();

                WithdrawalDetails details = withdrawalDetailsRepository
                        .findByTransaction(tx)
                        .orElseThrow(() -> new IllegalArgumentException("Withdrawal details missing for tx " + tx.getReference()));

                return WithdrawalResponseDto.builder()
                        .reference(tx.getReference())
                        .status(tx.getStatus())
                        .currency(tx.getCurrency())
                        .amountInKobo(tx.getAmount())
                        .bankCode(details.getBankCode())
                        .accountName(details.getAccountName())
                        .accountNumber(details.getAccountNumber())
                        .createdAt(tx.getCreatedAt())
                        .build();
            }
        }

        // Validate Account Status
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("User account is not Active");
        }

        // Load Wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(user.getId(), request.getCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Validate Balance =
        if (wallet.getBalance() < request.getAmountInKobo()) {
            throw new IllegalArgumentException("Insufficient Balance");
        }

        // Fraud Check
        fraudService.validateWithdrawal(user, request.getAmountInKobo());

        // Load Bank account
        BankAccount bankAccount = bankAccountRepository
                .findByIdAndUserId(request.getBankAccountId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Bank account not found"));

        // Generate withdrawal Reference
        String reference = "WD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // Create Transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .amount(request.getAmountInKobo())
                .currency(request.getCurrency())
                .reference(reference)
                .idempotencyKey(idempotencyKey)
                .build();

        // Save the transaction
        transaction = transactionRepository.save(transaction);

        WithdrawalDetails details = WithdrawalDetails.builder()
                .accountName(bankAccount.getAccountName())
                .accountNumber(bankAccount.getAccountNumber())
                .bankCode(bankAccount.getBankCode())
                .id(null)
                .transaction(transaction)
                .build();

        withdrawalDetailsRepository.save(details);

        return WithdrawalResponseDto.builder()
                .reference(transaction.getReference())
                .status(transaction.getStatus())
                .currency(transaction.getCurrency())
                .amountInKobo(transaction.getAmount())
                .bankCode(bankAccount.getBankCode())
                .accountName(bankAccount.getAccountName())
                .accountNumber(bankAccount.getAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    @Transactional
    public WithdrawalResponseDto completeWithdrawal(String reference) {

        // find transaction by reference
        Transaction transaction = transactionRepository.findByReferenceAndType(reference, TransactionType.WITHDRAWAL)
                .orElseThrow(() -> new IllegalArgumentException("Invalid reference"));

        // check if transaction is already Success/ Failed and return existing response
        if (transaction.getStatus() == TransactionStatus.SUCCESS ||
                transaction.getStatus() == TransactionStatus.FAILED) {

            return toResponse(transaction);
        }

        // Requires Pending
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Withdrawal must be pending to complete");
        }

        // Load wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(
                transaction.getUser().getId(),
                transaction.getCurrency()
        )
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));

        // Recheck Balance
        if (wallet.getBalance() < transaction.getAmount()){
            transaction.markFailed();
            transactionRepository.save(transaction);
            return toResponse(transaction);
        }

        // Debit wallet
        wallet.debit(transaction.getAmount());
        walletRepository.save(wallet);


        // Create Ledgers
        LedgerEntry walletDebit = LedgerEntry.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .wallet(wallet)
                .accountType(LedgerAccountType.USER_WALLET)
                .direction(LedgerEntryDirection.DEBIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        LedgerEntry externalCredit = LedgerEntry.builder()
                .transaction(transaction)
                .user(transaction.getUser())
                .wallet(null)
                .accountType(LedgerAccountType.EXTERNAL_PAYOUT)
                .direction(LedgerEntryDirection.CREDIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        ledgerEntryRepository.save(walletDebit);
        ledgerEntryRepository.save(externalCredit);

        // Mark Transaction as Success
        transaction.markSuccess();
        transactionRepository.save(transaction);

        // build Response
        return toResponse(transaction);
    }


    private WithdrawalResponseDto toResponse(Transaction tx) {
        WithdrawalDetails details = withdrawalDetailsRepository.findByTransaction(tx)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal details missing for tx " + tx.getReference()));

        return WithdrawalResponseDto.builder()
                .reference(tx.getReference())
                .status(tx.getStatus())
                .currency(tx.getCurrency())
                .amountInKobo(tx.getAmount())
                .bankCode(details.getBankCode())
                .accountNumber(details.getAccountNumber())
                .accountName(details.getAccountName())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}


