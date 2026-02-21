package com.marv.arionwallet.modules.funding.application;

import com.marv.arionwallet.modules.funding.domain.FundingDetails;
import com.marv.arionwallet.modules.funding.domain.FundingDetailsRepository;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.payments.application.PaymentProvider;
import com.marv.arionwallet.modules.policy.application.AccessPolicyService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import com.marv.arionwallet.modules.funding.presentation.FundWalletRequestDto;
import com.marv.arionwallet.modules.funding.presentation.InitiateFundingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FundingService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccessPolicyService accessPolicyService;
    private final FundingDetailsRepository fundingDetailsRepository;
    private final PaymentProvider paymentProvider;

    @Transactional
    public InitiateFundingResponseDto initiateFunding(User currentUser,
                                                      FundWalletRequestDto request,
                                                      String idempotencyKey) {

        accessPolicyService.requireActive(currentUser);

        // Find the users wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrency(currentUser.getId(), "NGN")
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        // Validate the amount;
        Long amount = request.getAmountInKobo();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        // Idempotency: return existing PENDING funding initiation
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Transaction> existingTx = transactionRepository.findByUserIdAndIdempotencyKeyAndType(currentUser.getId(), idempotencyKey, TransactionType.FUNDING);

            if (existingTx.isPresent()) {
                Transaction tx = existingTx.get();

                FundingDetails details = fundingDetailsRepository.
                        findByTransaction(tx)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Funding details missing for tx " + tx.getReference()));

                return InitiateFundingResponseDto.builder()
                        .reference(tx.getReference())
                        .currency(tx.getCurrency())
                        .status(tx.getStatus())
                        .amountInKobo(tx.getAmount())
                        .paymentUrl(details.getPaymentUrl())
                        .createdAt(tx.getCreatedAt())
                        .build();

            }
        }

        // Generate payment reference
        String reference = "FUND-" + UUID.randomUUID()
                .toString()
                .replace("_", "")
                .substring(0, 12);

        // Create PENDING transaction
        Transaction transaction = Transaction.builder()
                .user(currentUser)
                .type(TransactionType.FUNDING)
                .status(TransactionStatus.PENDING)
                .amount(amount)
                .currency(wallet.getCurrency())
                .reference(reference)
                .description(request.getDescription())
                .idempotencyKey(idempotencyKey)
                .build();

        transaction = transactionRepository.save(transaction);

        PaymentProvider.InitPaymentResult init = paymentProvider.initializePayment(
                new PaymentProvider.InitPaymentRequest(
                        transaction.getReference(),
                        currentUser.getId(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        currentUser.getEmail()
                )
        );

        FundingDetails details = FundingDetails.builder()
                .transaction(transaction)
                .providerReference(init.providerReference())
                .paymentUrl(init.providerReference())
                .build();

        fundingDetailsRepository.save(details);

        // Return the information the client/gateway would use
        return InitiateFundingResponseDto.builder()
                .reference(transaction.getReference())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus())
                .amountInKobo(transaction.getAmount())
                .paymentUrl(details.getPaymentUrl())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
