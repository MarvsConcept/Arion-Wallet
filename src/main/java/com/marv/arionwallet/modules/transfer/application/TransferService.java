package com.marv.arionwallet.modules.transfer.application;

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
import com.marv.arionwallet.modules.user.domain.UserRepository;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FraudService fraudService;

    @Transactional
    public TransferResponseDto transfer(User sender, TransferRequestDto request, String idempotencyKey) {

        // Currency is NGN
        if (!request.getCurrency().trim().equalsIgnoreCase("NGN")) {
            throw new IllegalArgumentException("Only NGN is supported");
        }

        // Amount Validation
        if (request.getAmountInKobo() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // Load Recipient by Account Number
        User recipient = userRepository.findByAccountNumber(request.getRecipientAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Load Sender Wallet
        Wallet senderWallet = walletRepository.findByUserIdAndCurrencyForUpdate(sender.getId(), "NGN")
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Load recipient wallet
        Wallet recipientWallet = walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN")
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Narration
        String narration = request.getNarration();
        if (narration == null || narration.isBlank()) {
            narration = "P2P Transfer";
        }

        // Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existingTx = transactionRepository.findByUserIdAndIdempotencyKey(sender.getId(),idempotencyKey);

            if (existingTx.isPresent()) {
                Transaction tx = existingTx.get();

                return TransferResponseDto.builder()
                        .reference(tx.getReference())
                        .amountInKobo(tx.getAmount())
                        .currency(tx.getCurrency())
                        .senderFullName("From: " + sender.getFirstName() + " " + sender.getLastName())
                        .senderAccountNumber(sender.getAccountNumber())
                        .recipientFullName("To: " + recipient.getFirstName() + " " + recipient.getLastName())
                        .recipientAccountNumber(recipient.getAccountNumber())
                        .senderNewBalance(senderWallet.getBalance())
                        .recipientNewBalance(recipientWallet.getBalance())
                        .narration(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .build();
            }
        }

        fraudService.validateTransfer(sender, request.getAmountInKobo());

        // Sender Balance is greater then amount
        if (senderWallet.getBalance() < request.getAmountInKobo()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Generate payment reference
        String reference = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // Build Transaction
        Transaction transaction = Transaction.builder()
                .user(sender)
                .amount(request.getAmountInKobo())
                .currency(request.getCurrency())
                .reference(reference)
                .description(narration)
                .idempotencyKey(idempotencyKey)
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        // Save the transaction
        transaction = transactionRepository.save(transaction);

        // Debit the sender
        senderWallet.debit(request.getAmountInKobo());

        // Credit the recipient
        recipientWallet.credit(request.getAmountInKobo());

        // Save to wallet
        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

        // Created Ledger Entry using the saved transaction
        LedgerEntry debitWalletEntry = LedgerEntry.builder()
                .transaction(transaction)
                .user(sender)
                .wallet(senderWallet)
                .accountType(LedgerAccountType.USER_WALLET)
                .direction(LedgerEntryDirection.DEBIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        LedgerEntry creditWalletEntry = LedgerEntry.builder()
                .transaction(transaction)
                .user(recipient)
                .wallet(recipientWallet)
                .accountType(LedgerAccountType.USER_WALLET)
                .direction(LedgerEntryDirection.CREDIT)
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .build();

        // Save Ledger Entry
        ledgerEntryRepository.save(debitWalletEntry);
        ledgerEntryRepository.save(creditWalletEntry);


        return TransferResponseDto.builder()
                .reference(reference)
                .amountInKobo(request.getAmountInKobo())
                .currency(request.getCurrency())
                .senderFullName("From: " + sender.getFirstName() + " " + sender.getLastName())
                .senderAccountNumber(sender.getAccountNumber())
                .recipientFullName("To: " + recipient.getFirstName() + " " + recipient.getLastName())
                .recipientAccountNumber(request.getRecipientAccountNumber())
                .senderNewBalance(senderWallet.getBalance())
                .recipientNewBalance(recipientWallet.getBalance())
                .narration(narration)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
