package com.marv.arionwallet.modules.wallet.application;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import com.marv.arionwallet.modules.wallet.presentation.FundWalletRequestDto;
import com.marv.arionwallet.modules.wallet.presentation.FundWalletResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public FundWalletResponseDto fundWallet(User currentUser, FundWalletRequestDto request) {

        // Find the users wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(currentUser.getId(), "NGN")
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        // Validate the amount;
        Long amount = request.getAmountInKobo();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Generate a reference
        String reference = "FUND-" + UUID.randomUUID().toString().replace("_", "").substring(0, 12);

        // Create SUCCESS transaction for now(Sync funding)
        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .type(TransactionType.FUNDING)
                .status(TransactionStatus.SUCCESS)
                .amount(amount)
                .currency(wallet.getCurrency())
                .reference(reference)
                .description(request.getDescription())
                .build();

        transactionRepository.save(transaction);

        // Credit wallet
        wallet.credit(amount);
        walletRepository.save(wallet);

        // Build Response;
        return FundWalletResponseDto.builder()
                .reference(reference)
                .currency(wallet.getCurrency())
                .amountInKobo(amount)
                .newBalanceInKobo(wallet.getBalance())
                .build();
    }
}
