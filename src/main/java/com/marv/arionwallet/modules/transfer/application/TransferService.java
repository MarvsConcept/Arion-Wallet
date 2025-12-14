package com.marv.arionwallet.modules.transfer.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
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

    @Transactional
    public TransferResponseDto transfer(User sender, TransferRequestDto request) {

        // Currency is NGN
        if (!request.getCurrency().trim().equalsIgnoreCase("NGN")) {
            throw new IllegalArgumentException("Only NGN is supported");
        }

        // Load Recipient by Account Number
        User recipient = userRepository.findByAccountNumber(request.getRecipientAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent transfer to self
        if (sender.getId().equals(recipient.getId())) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        // Load Sender Wallet
        Wallet senderWallet = walletRepository.findByUserIdAndCurrency(sender.getId(), "NGN")
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Load recipient wallet
        Wallet recipientWallet = walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN")
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Sender Balance is greater then amount
        if (senderWallet.getBalance() < request.getAmountInKobo()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // Generate payment reference
        String reference = "TX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        Transaction transaction = Transaction.builder()
                .user(sender)
                .amount(request.getAmountInKobo())
                .currency(request.getCurrency())
                .reference(reference)
                .description(request.getNarration())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);

        senderWallet.debit(request.getAmountInKobo());
        recipientWallet.credit(request.getAmountInKobo());

        walletRepository.save(senderWallet);
        walletRepository.save(recipientWallet);

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
                .narration(request.getNarration())
                .build();
    }
}
