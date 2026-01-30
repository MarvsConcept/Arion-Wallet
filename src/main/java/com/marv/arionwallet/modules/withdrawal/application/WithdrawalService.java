package com.marv.arionwallet.modules.withdrawal.application;

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

                return WithdrawalResponseDto.builder()
                        .reference(tx.getReference())
                        .status(tx.getStatus())
                        .currency(tx.getCurrency())
//                        .destination(tx.getDestination())
                        .amountInKobo(tx.getAmount())
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
//                .destination(request.getDestination())
                .idempotencyKey(idempotencyKey)
                .build();

        // Save the transaction
        transaction = transactionRepository.save(transaction);

        return WithdrawalResponseDto.builder()
                .reference(transaction.getReference())
                .status(transaction.getStatus())
                .currency(transaction.getCurrency())
                .amountInKobo(transaction.getAmount())
//                .destination(transaction.getDestination())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}


