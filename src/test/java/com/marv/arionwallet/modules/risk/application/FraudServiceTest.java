package com.marv.arionwallet.modules.risk.application;

import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FraudServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private FraudService fraudService;

    @BeforeEach
    void setUp() {
        fraudService = new FraudService(transactionRepository);
    }

    @Test
    void validateTransfer_shouldThrow_whenAmountExceedSingleLimit() {
        // Arrange: create a user with BASIC KYC
        User user = User.builder()
                .email("test@example.com")
                .phone("08000000000")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .accountNumber("2999990000")
                .kycLevel(KycLevel.BASIC)
                .build();

        long tooBigAmount = 3_000_000L; // ₦30,000 in kobo

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fraudService.validateTransfer(user, tooBigAmount)
        );

        assertEquals("Amount exceeded per transaction limit for your KYC level", ex.getMessage());
    }

    @Test
    void validateTransfer_shouldThrow_whenDailyLimitExceeded() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .phone("08000000000")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .accountNumber("2999990000")
                .kycLevel(KycLevel.BASIC)
                .build();

        long todayTotal = 4_900_000L;
        long amount = 200_000L;

        when(transactionRepository.sumSuccessfulTransfersForUserBetween(
                eq(user.getId()),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(todayTotal);

        // Act + Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> fraudService.validateTransfer(user, amount)
        );

        assertEquals("Daily transfer limit exceeded for your KYC level", ex.getMessage());

        // verify repo was queried exactly once
        verify(transactionRepository, times(1)).sumSuccessfulTransfersForUserBetween(
                eq(user.getId()),
                any(Instant.class),
                any(Instant.class)
        );
    }

    @Test
    void validateTransfer_shouldNotThrow_whenWithinDailyLimit() {

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .phone("08000000000")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .accountNumber("2999990000")
                .kycLevel(KycLevel.BASIC)
                .build();

        long todayTotal = 4_000_000L;
        long amount = 200_000L;

        when(transactionRepository.sumSuccessfulTransfersForUserBetween(
                eq(user.getId()),
                any(Instant.class),
                any(Instant.class)
        )).thenReturn(todayTotal);

        assertDoesNotThrow(() -> fraudService.validateTransfer(user, amount));

        verify(transactionRepository, times(1))
                .sumSuccessfulTransfersForUserBetween(eq(userId), any(Instant.class), any(Instant.class));

    }

}
