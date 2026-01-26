package com.marv.arionwallet.modules.transfer.application;

import com.marv.arionwallet.modules.ledger.domain.LedgerEntry;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryDirection;
import com.marv.arionwallet.modules.ledger.domain.LedgerEntryRepository;
import com.marv.arionwallet.modules.risk.application.FraudService;
import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transfer.presentation.TransferRequestDto;
import com.marv.arionwallet.modules.transfer.presentation.TransferResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private FraudService fraudService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                userRepository,
                walletRepository,
                transactionRepository,
                ledgerEntryRepository,
                fraudService
        );
    }

    @Test
    void transfer_shouldReturnExistingTransaction_whenIdempotencyKeyReused(){

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .build();

        // Dummy Idempotency key
        String idempotencyKey = "idem-123";

        // Dummy Transaction representing a previous transaction
        Transaction existingTx = Transaction.builder()
                .id(UUID.randomUUID())
                .reference("TX-EXISTING")
                .amount(200_000L)
                .currency("NGN")
                .description("P2P Transfer")
                .createdAt(Instant.now())
                .build();

        // Tell Mockito to return the existing transaction
        when(transactionRepository.findByUserIdAndIdempotencyKey(user.getId(), idempotencyKey))
                .thenReturn(Optional.of(existingTx));

        // ACT - Calling the method i want to test

        // Create a minimal transfer request
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientAccountNumber("2999990001");
        request.setAmountInKobo(200_000L);
        request.setCurrency("NGN");
        request.setNarration("P2P Transfer");

        // Mock the remaining dependency before idempotency
        User recipient = User.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Doe")
                .accountNumber("2999990001")
                .build();

        Wallet senderWallet = Wallet.builder()
                .balance(1_000_000L)
                .build();

        Wallet recipientWallet = Wallet.builder()
                .balance(500_000L)
                .build();

        when(userRepository.findByAccountNumber("2999990001"))
                .thenReturn(Optional.of(recipient));

        when(walletRepository.findByUserIdAndCurrencyForUpdate(user.getId(), "NGN"))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN"))
                .thenReturn(Optional.of(recipientWallet));

        // Call the method
        TransferResponseDto response = transferService.transfer(user, request, idempotencyKey);

        // Assertions
        // prove that the method didn't generate a new reference
        assertEquals("TX-EXISTING", response.getReference());

        // Assert that fraud validation did not run
        verify(fraudService, never()).validateTransfer(any(), anyLong());

        // Assert that no transaction was saved
        verify(transactionRepository, never()).save(any());

        // Assert wallets were not changed
        verify(walletRepository, never()).save(any());

        // Assert that no ledger entries were written
        verify(ledgerEntryRepository, never()).save(any());


        // Verify idempotency lookup was actually used
        verify(transactionRepository, times(1))
                .findByUserIdAndIdempotencyKey(user.getId(), idempotencyKey);

    }

    @Test
    void transfer_shouldThrow_whenInsufficientBalance_andNotSaveAnything() {

        // Arrange
        // Create Sender
        User sender = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .accountNumber("2900000011")
                .build();

        // Create recipient
        User recipient = User.builder()
                .id(UUID.randomUUID())
                .accountNumber("2999990001")
                .build();

        // Create transfer Request
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientAccountNumber(recipient.getAccountNumber());
        request.setAmountInKobo(200_000L);
        request.setCurrency("NGN");
        request.setNarration("P2P Transfer");

        // Create Sender Wallet
        Wallet senderWallet = Wallet.builder()
                .balance(100_000L)
                .build();

        // Create Recipient Wallet
        Wallet recipientWallet = Wallet.builder()
                .balance(300_000L)
                .build();

        // Mock the repositories
        when(userRepository.findByAccountNumber(recipient.getAccountNumber()))
                .thenReturn(Optional.of(recipient));

        when(walletRepository.findByUserIdAndCurrencyForUpdate(sender.getId(), "NGN"))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN"))
                .thenReturn(Optional.of(recipientWallet));

        doNothing().when(fraudService).validateTransfer(sender, request.getAmountInKobo());

        // Act
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(sender, request, null)
        );

        assertEquals("Insufficient balance", ex.getMessage());

        // Verify no saves happened
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));

        //verify
        verify(fraudService, times(1)).validateTransfer(sender, request.getAmountInKobo());

    }

    @Test
    void transfer_shouldThrow_whenFraudBlocks_andNotSaveAnything() {

        //Arrange
        // Create Sender
        User sender = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .accountNumber("2900000011")
                .build();

        // Create recipient
        User recipient = User.builder()
                .id(UUID.randomUUID())
                .accountNumber("2999990001")
                .build();

        // Create transfer Request
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientAccountNumber(recipient.getAccountNumber());
        request.setAmountInKobo(200_000L);
        request.setCurrency("NGN");
        request.setNarration("P2P Transfer");

        // Create Sender Wallet
        Wallet senderWallet = Wallet.builder()
                .balance(1_000_000L)
                .build();

        // Create Recipient Wallet
        Wallet recipientWallet = Wallet.builder()
                .balance(300_000L)
                .build();

        when(userRepository.findByAccountNumber(recipient.getAccountNumber()))
                .thenReturn(Optional.of(recipient));

        when(walletRepository.findByUserIdAndCurrencyForUpdate(sender.getId(), "NGN"))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN"))
                .thenReturn(Optional.of(recipientWallet));

        doThrow(new IllegalArgumentException("Daily transfer limit exceeded for your KYC level"))
                .when(fraudService)
                .validateTransfer(sender, request.getAmountInKobo());

        // ACT
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(sender, request, null)
        );

        // ASSERT
        assertEquals("Daily transfer limit exceeded for your KYC level", ex.getMessage());


        //Verify
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));

        verify(fraudService, times(1)).validateTransfer(sender, request.getAmountInKobo());

        verify(walletRepository, times(1))
                .findByUserIdAndCurrencyForUpdate(sender.getId(), "NGN");
        verify(walletRepository, times(1))
                .findByUserIdAndCurrency(recipient.getId(), "NGN");
    }

    @Test
    void transfer_shouldThrow_whenCurrencyNotNGN_andNotCallDependencies() {
        User sender = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .accountNumber("2900000011")
                .build();

        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientAccountNumber("2900000022");
        request.setAmountInKobo(200_000L);
        request.setCurrency("USD");
        request.setNarration("P2P Transfer");

        // ACT
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(sender, request, null)
        );

        // ASSERT
        assertEquals("Only NGN is supported", ex.getMessage());

        // Verify nothing else was called
        verifyNoInteractions(userRepository, walletRepository, transactionRepository, ledgerEntryRepository, fraudService);
    }


    @Test
    void transfer_shouldSucceed_andMoveMoney_andWriteLedger() {

        // Arrange
        User sender = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test")
                .lastName("User")
                .accountNumber("2900000011")
                .build();

        User recipient = User.builder()
                .id(UUID.randomUUID())
                .firstName("Test2")
                .lastName("user2")
                .accountNumber("2900000022")
                .build();

        // Create transfer Request
        TransferRequestDto request = new TransferRequestDto();
        request.setRecipientAccountNumber(recipient.getAccountNumber());
        request.setAmountInKobo(200_000L);
        request.setCurrency("NGN");
        request.setNarration("P2P Transfer");

        // Create wallets
        Wallet senderWallet = Wallet.builder()
                .balance(1_000_000L)
                .build();

        Wallet recipientWallet = Wallet.builder()
                .balance(300_000L)
                .build();

        // Mocks
        when(userRepository.findByAccountNumber(recipient.getAccountNumber()))
                .thenReturn(Optional.of(recipient));

        when(walletRepository.findByUserIdAndCurrencyForUpdate(sender.getId(), "NGN"))
                .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findByUserIdAndCurrency(recipient.getId(), "NGN"))
                .thenReturn(Optional.of(recipientWallet));

        doNothing().when(fraudService).validateTransfer(sender, request.getAmountInKobo());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));


        // ACT
        TransferResponseDto response = transferService.transfer(sender, request, null);

        assertEquals(800_000L, senderWallet.getBalance());
        assertEquals(500_000L, recipientWallet.getBalance());

        assertEquals(800_000L, response.getSenderNewBalance());
        assertEquals(500_000L, response.getRecipientNewBalance());

        // Verify saves happened
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));

        verify(fraudService, times(1)).validateTransfer(sender, request.getAmountInKobo());

        // Check Ledger entries are correct(Debit + Credit
        ArgumentCaptor<LedgerEntry> captor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerEntryRepository, times(2)).save(captor.capture());

        List<LedgerEntry> savedEntries = captor.getAllValues();
        assertEquals(2, savedEntries.size());

        boolean hasDebit = savedEntries.stream().anyMatch(e ->
                e.getDirection() == LedgerEntryDirection.DEBIT &&
                        e.getUser().equals(sender) &&
                        Objects.equals(e.getAmount(), 200_000L)

        );

        boolean hasCredit = savedEntries.stream().anyMatch(e ->
                e.getDirection() == LedgerEntryDirection.CREDIT &&
                        e.getUser().equals(recipient) &&
                        Objects.equals(e.getAmount(), 200_000L)
        );

        assertTrue(hasDebit);
        assertTrue(hasCredit);
    }

}
