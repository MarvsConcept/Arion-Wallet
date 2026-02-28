package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerAccountType;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.fraud.application.FraudService;
import com.marv.arionwallet.modules.payout.application.PayoutProvider;
import com.marv.arionwallet.modules.payout.presentation.PayoutStatus;
import com.marv.arionwallet.modules.payout.presentation.PayoutWebhookStatus;
import com.marv.arionwallet.modules.policy.application.AccessPolicyService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import com.marv.arionwallet.modules.wallet.application.HoldService;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import com.marv.arionwallet.modules.banking.domain.BankAccount;
import com.marv.arionwallet.modules.banking.domain.BankAccountRepository;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetails;
import com.marv.arionwallet.modules.withdrawal.domain.WithdrawalDetailsRepository;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalDetailsResponseDto;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalHistoryItemDto;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalRequestDto;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final WithdrawalDetailsRepository withdrawalDetailsRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final WalletRepository walletRepository;
    private final FraudService fraudService;
    private final AccessPolicyService accessPolicyService;
    private final PayoutProvider payoutProvider;
    private final HoldService holdService;

    @Transactional
    public WithdrawalResponseDto requestWithdrawal(User user,
                                                   WithdrawalRequestDto request,
                                                   String idempotencyKey) {

        accessPolicyService.requireActive(user);
        accessPolicyService.requireKycAtLeast(user, KycLevel.BASIC);

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
            Optional<Transaction> existingTx =
                    transactionRepository.findByUserIdAndIdempotencyKeyAndType(
                            user.getId(),
                            idempotencyKey,
                            TransactionType.WITHDRAWAL
                    );

            if (existingTx.isPresent()) {
                return toResponse(existingTx.get());
            }
        }

        // Load Wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(user.getId(), request.getCurrency())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Validate Balance =
        long available = holdService.availableBalance(wallet);
        if (available < request.getAmountInKobo()) {
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

        holdService.createActiveHold(wallet, transaction.getId(), transaction.getAmount(), transaction.getCurrency());

        WithdrawalDetails details = WithdrawalDetails.builder()
                .accountName(bankAccount.getAccountName())
                .accountNumber(bankAccount.getAccountNumber())
                .bankCode(bankAccount.getBankCode())
                .id(null)
                .transaction(transaction)
                .build();

        details = withdrawalDetailsRepository.save(details);

        // Initiate payout with provider
        PayoutProvider.PayoutRequest payoutRequest = new PayoutProvider.PayoutRequest(
                bankAccount.getId(),
                transaction.getReference(),
                details.getBankCode(),
                details.getAccountNumber(),
                details.getAccountName(),
                transaction.getAmount(),
                transaction.getCurrency()
        );

        try {
            PayoutProvider.PayoutResult result = payoutProvider.initiatePayout(payoutRequest);
            log.info("Payout result: ref={}, status={}, providerRef={}",
                    transaction.getReference(),
                    result.status(),
                    result.providerReference());


            // Save provider reference once
            details.setProviderReferenceIfAbsent(result.providerReference());
            withdrawalDetailsRepository.save(details);

            // Optional: if provider immediately says FAILED, mark tx FAILED
            if (result.status() == PayoutStatus.FAILED) {
                log.error("Payout failed for {} — providerRef={}",
                        transaction.getReference(),
                        result.providerReference());
                transaction.markFailed();
                holdService.releaseHold(transaction.getId());
                transactionRepository.save(transaction);
            }

            // Return PENDING/FAILED snapshot (webhook is source of truth for SUCCESS)
            return toResponse(transaction);

        } catch ( Exception ex) {
            log.error("Payout exception", ex);
            transaction.markFailed();
            transactionRepository.save(transaction);
            holdService.releaseHold(transaction.getId());
            // optionally store error message later
            return toResponse(transaction);
        }
    }


    @Transactional
    public WithdrawalResponseDto settleWithdrawalFromWebhook(String providerReference,
                                                             PayoutWebhookStatus status) {


        WithdrawalDetails details = withdrawalDetailsRepository.findByProviderReference(providerReference)
                .orElseThrow(() -> new IllegalArgumentException("Unknown provider reference"));

        // LOCK the transaction to avoid double-settlement on concurrent webhooks
        Transaction transaction = transactionRepository.findByIdForUpdate(details.getTransaction().getId())
                .orElseThrow(() -> new IllegalStateException("Transaction missing"));

//        Transaction transaction = details.getTransaction();

        // If already finalized, return existing...Idempotent
        if (transaction.getStatus() == TransactionStatus.SUCCESS || transaction.getStatus() == TransactionStatus.FAILED) {
            return toResponse(transaction);
        }
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Withdrawal must be PENDING to settle");
        }
        if (status == PayoutWebhookStatus.FAILED) {
            transaction.markFailed();
            transactionRepository.save(transaction);
            holdService.releaseHold(transaction.getId());
            return toResponse(transaction);
        }

        return finalizeSuccessfulWithdrawal(transaction);

    }

    private WithdrawalResponseDto finalizeSuccessfulWithdrawal(Transaction transaction) {

        // Ensure pending before debit
        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Withdrawal must be PENDING to finalize");
        }

        // Lock wallet
        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(
                        transaction.getUser().getId(),
                        transaction.getCurrency()
                ).orElseThrow(() -> new IllegalArgumentException("Wallet not found for user"));

        // Ensure hold is ACTIVE (means money was reserved
        holdService.requireActiveHold(transaction.getId());

        // if user is frozen, fail it and release hold
        if (transaction.getUser().getStatus() != UserStatus.ACTIVE) {
            transaction.markFailed();
            transactionRepository.save(transaction);
            holdService.releaseHold(transaction.getId());
            return toResponse(transaction);
        }

        // fraud check
        fraudService.validateWithdrawal(transaction.getUser(), transaction.getAmount());

//        // Balance Check
//        if (wallet.getBalance() < transaction.getAmount()){
//            transaction.markFailed();
//            transactionRepository.save(transaction);
//            holdService.releaseHold(transaction.getId());
//            return toResponse(transaction);
//        }

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

        // Consume hold
        holdService.consumeHold(transaction.getId());

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
                .accountNumber(maskAccount(details.getAccountNumber()))
                .accountName(details.getAccountName())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    public WithdrawalDetailsResponseDto getWithdrawal(User user, String reference) {

        WithdrawalDetails wd =  withdrawalDetailsRepository.
                findByTransaction_User_IdAndTransaction_Reference(user.getId(), reference)
                .orElseThrow(() -> new IllegalArgumentException("Withdrawal not found"));

        Transaction tx = wd.getTransaction();

        return WithdrawalDetailsResponseDto.builder()
                .reference(tx.getReference())
                .status(tx.getStatus())
                .amountInKobo(tx.getAmount())
                .currency(tx.getCurrency())
                .bankCode(wd.getBankCode())
                .accountName(wd.getAccountName())
                .accountNumberMasked(maskAccount(wd.getAccountNumber()))
                .createdAt(tx.getCreatedAt())
                .build();

    }

    public Page<WithdrawalHistoryItemDto> listWithdrawals(User user, Pageable pageable) {

//        Pageable pageable = PageRequest.of(page, size);

        Page<WithdrawalDetails> wdPage =
                withdrawalDetailsRepository.findByTransaction_User_IdOrderByTransaction_CreatedAtDesc(user.getId(), pageable);

        wdPage = withdrawalDetailsRepository.
                findByTransaction_User_IdOrderByTransaction_CreatedAtDesc(user.getId(), pageable);

        return wdPage.map(wd -> {
            Transaction tx = wd.getTransaction();

            return WithdrawalHistoryItemDto.builder()
                    .reference(tx.getReference())
                    .status(tx.getStatus())
                    .amountInKobo(tx.getAmount())
                    .currency(tx.getCurrency())
                    .bankCode(wd.getBankCode())
                    .accountName(wd.getAccountName())
                    .accountNumberMasked(maskAccount(wd.getAccountNumber()))
                    .createdAt(tx.getCreatedAt())
                    .build();
        });

    }

    private String maskAccount(String accountNumber) {

            if (accountNumber == null || accountNumber.isBlank())
                return null;

            String trimmed = accountNumber.trim();
            if (trimmed.length() <= 4) return "****";

            String last4 = trimmed.substring(trimmed.length() - 4);
            return "******" + last4;
        }
}



