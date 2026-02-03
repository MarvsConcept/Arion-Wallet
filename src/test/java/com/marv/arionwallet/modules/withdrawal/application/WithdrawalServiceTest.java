package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.risk.application.FraudService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private BankAccountRepository bankAccountRepository;
    @Mock
    private WithdrawalDetailsRepository withdrawalDetailsRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private FraudService fraudService;

    private WithdrawalService withdrawalService;

    @BeforeEach
    void setUp() {
         withdrawalService = new WithdrawalService(
                transactionRepository,
                bankAccountRepository,
                withdrawalDetailsRepository,
                ledgerEntryRepository,
                walletRepository,
                fraudService
                );
    }

    @Test
    void requestWithdrawal_shouldReturnExisting_whenIdempotencyKeyReused_includingDestination() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        // Dummy Idempotency Key
        String idempotencyKey = "idem-1234";

        // Dummy BankId
        UUID bankId = UUID.randomUUID();

        // Dummy Transaction representing a previous withdrawal
        Transaction existingTx = Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .reference("WD-EXISTING")
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.PENDING)
                .amount(200_000L)
                .currency("NGN")
                .createdAt(Instant.now())
                .build();


        WithdrawalDetails details = WithdrawalDetails.builder()
                .transaction(existingTx)
                .bankCode("123")
                .accountName("Marv")
                .accountNumber("123456789")
                .build();

        // Tell Mockito to return the exiting transaction
        when(transactionRepository.findByUserIdAndIdempotencyKeyAndType(
                userId, idempotencyKey, TransactionType.WITHDRAWAL))
                .thenReturn(Optional.of(existingTx));

        when(withdrawalDetailsRepository.findByTransaction(existingTx))
                .thenReturn(Optional.of(details));

        // ACT - Calling the method.

        // Withdrawal request
        WithdrawalRequestDto request = WithdrawalRequestDto.builder()
                .amountInKobo(200_000L)
                .currency("NGN")
                .bankAccountId(bankId)
                .build();

        // Call the method
        WithdrawalResponseDto response = withdrawalService.requestWithdrawal(user, request, idempotencyKey);

        // Assert
        assertEquals("WD-EXISTING", response.getReference());
        assertEquals("123", response.getBankCode());
        assertEquals("Marv", response.getAccountName());
        assertEquals("123456789", response.getAccountNumber());

        // Safety verifies
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(withdrawalDetailsRepository, never()).save(any(WithdrawalDetails.class));
        verifyNoInteractions(walletRepository, bankAccountRepository, fraudService, ledgerEntryRepository);

        // Verify the two rep calls happened once
        verify(transactionRepository, times(1))
                .findByUserIdAndIdempotencyKeyAndType(userId, idempotencyKey, TransactionType.WITHDRAWAL);

        verify(withdrawalDetailsRepository, times(1)).findByTransaction(existingTx);

    }


    @Test
    void requestWithdrawal_shouldCreatePendingTransaction_andSaveWithdrawalDetailsSnapshot() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();

        // Dummy BankId
        UUID bankId = UUID.randomUUID();

        // Dummy Idempotency
        String idempotencyKey = "idem-new";

        // Withdrawal Request
        WithdrawalRequestDto request = WithdrawalRequestDto.builder()
                .amountInKobo(200_000L)
                .currency("NGN")
                .bankAccountId(bankId)
                .build();


        // Mock
        Wallet wallet = Wallet.builder()
                .balance(1_000_000L)
                .build();

        BankAccount bankAccount = BankAccount.builder()
                .id(bankId)
                .accountName("Marv")
                .accountNumber("123456789")
                .bankCode("123")
                .build();

        when(walletRepository.findByUserIdAndCurrencyForUpdate(userId, "NGN"))
                .thenReturn(Optional.of(wallet));
        when(transactionRepository.findByUserIdAndIdempotencyKeyAndType(userId, idempotencyKey, TransactionType.WITHDRAWAL))
                .thenReturn(Optional.empty());
        when(bankAccountRepository.findByIdAndUserId(bankId, userId))
                .thenReturn(Optional.of(bankAccount));
        doNothing().when(fraudService).validateWithdrawal(user, request.getAmountInKobo());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        when(withdrawalDetailsRepository.save(any(WithdrawalDetails.class))).thenAnswer(inv -> inv.getArgument(0));


        // ACT
        WithdrawalResponseDto response = withdrawalService.requestWithdrawal(
                user, request, idempotencyKey);


        // ASSERT
        assertNotNull(response.getReference());
        assertTrue(response.getReference().startsWith("WD-"));
        assertEquals(TransactionStatus.PENDING, response.getStatus());
        assertEquals(200_000L, response.getAmountInKobo());
        assertEquals("NGN", response.getCurrency());
        assertEquals("123", response.getBankCode());
        assertEquals("Marv", response.getAccountName());
        assertEquals("123456789", response.getAccountNumber());

        // Capture Transaction(what was saved)
        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(txCaptor.capture());
        Transaction savedTx = txCaptor.getValue();

        assertEquals(TransactionType.WITHDRAWAL, savedTx.getType());
        assertEquals(TransactionStatus.PENDING, savedTx.getStatus());
        assertEquals(200_000L, savedTx.getAmount());
        assertEquals("NGN", savedTx.getCurrency());
        assertEquals(idempotencyKey, savedTx.getIdempotencyKey());
        assertTrue(savedTx.getReference().startsWith("WD-"));

        // Capture Withdrawal details
        ArgumentCaptor<WithdrawalDetails> detailsCaptor = ArgumentCaptor.forClass(WithdrawalDetails.class);
        verify(withdrawalDetailsRepository).save(detailsCaptor.capture());
        WithdrawalDetails savedDetails = detailsCaptor.getValue();

        assertEquals("123", savedDetails.getBankCode());
        assertEquals("Marv", savedDetails.getAccountName());
        assertEquals("123456789", savedDetails.getAccountNumber());
        assertSame(savedTx, savedDetails.getTransaction()); // or assertEquals if equals() is implemented


        // verify, no ledger or wallet debit
        verify(ledgerEntryRepository, never()).save(any());
        verify(walletRepository, never()).save(any()); // requestWithdrawal doesn't save wallet currently

        //  fraud is called
        verify(fraudService).validateWithdrawal(user, 200_000L);

    }

    @Test
    void requestWithdrawal_shouldThrow_whenInsufficientBalance() {

        UUID userId = UUID.randomUUID();

        // Dummy Idempotency Key
        String idempotencyKey = "idem-1234";
        // Dummy BankId
        UUID bankId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();

        // Withdrawal request
        WithdrawalRequestDto request = WithdrawalRequestDto.builder()
                .amountInKobo(200_000L)
                .currency("NGN")
                .bankAccountId(bankId)
                .build();

        // Mock
        Wallet wallet = Wallet.builder()
                .balance(100_000L)
                .build();

        when(walletRepository.findByUserIdAndCurrencyForUpdate(userId, "NGN"))
                .thenReturn(Optional.of(wallet));
        when(transactionRepository.findByUserIdAndIdempotencyKeyAndType(userId, idempotencyKey, TransactionType.WITHDRAWAL))
                .thenReturn(Optional.empty());

        // ACT
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> withdrawalService.requestWithdrawal(user, request, idempotencyKey)
        );

        // ASSERT
        assertEquals("Insufficient Balance", ex.getMessage());

        // Verify
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(withdrawalDetailsRepository, never()).save(any(WithdrawalDetails.class));
        verifyNoInteractions(bankAccountRepository, fraudService, ledgerEntryRepository);

        verify(transactionRepository, times(1))
                .findByUserIdAndIdempotencyKeyAndType(userId, idempotencyKey, TransactionType.WITHDRAWAL);


    }
}