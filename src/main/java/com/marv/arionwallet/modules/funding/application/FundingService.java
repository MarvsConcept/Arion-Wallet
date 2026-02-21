package com.marv.arionwallet.modules.funding.application;

import com.marv.arionwallet.modules.funding.domain.FundingDetails;
import com.marv.arionwallet.modules.funding.domain.FundingDetailsRepository;
import com.marv.arionwallet.modules.funding.presentation.CompleteFundingResponseDto;
import com.marv.arionwallet.modules.payments.presentation.PaymentWebhookStatus;
import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
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

    @Transactional
    public CompleteFundingResponseDto handleWebhook(String providerReference, PaymentWebhookStatus status) {

        FundingDetails details = fundingDetailsRepository.findByProviderReference(providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider reference"));

        Transaction transaction = details.getTransaction();

        // Idempotent webhook
        if (transaction.getStatus() == TransactionStatus.SUCCESS || transaction.getStatus() == TransactionStatus.FAILED) {
            return buildCompleteResponse(transaction);
        }

        // Transaction must be Pending
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Funding must be pending to settle");
        }
        if (status == PaymentWebhookStatus.FAILED) {
            transaction.markFailed();
            transactionRepository.save(transaction);
            return buildCompleteResponse(transaction);
        }

        // Load the wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(
                transaction.getUser().getId(),
                transaction.getCurrency()
        ).orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        // Mark Transaction as Success and save
        transaction.markSuccess();
        transactionRepository.save(transaction);

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

        // Save Ledgers
        ledgerEntryRepository.save(externalEntry);
        ledgerEntryRepository.save(walletEntry);

        return CompleteFundingResponseDto.builder()
                .reference(transaction.getReference())
                .currency(transaction.getCurrency())
                .amountInKobo(transaction.getAmount())
                .newBalanceInKobo(wallet.getBalance())
                .build();
    }

    private CompleteFundingResponseDto buildCompleteResponse(Transaction transaction) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(
                transaction.getUser().getId(),
                transaction.getCurrency()
        ).orElseThrow(() -> new IllegalStateException("Wallet not found for user"));

        return CompleteFundingResponseDto.builder()
                .reference(transaction.getReference())
                .currency(transaction.getCurrency())
                .amountInKobo(transaction.getAmount())
                .newBalanceInKobo(wallet.getBalance())
                .build();
    }

}
