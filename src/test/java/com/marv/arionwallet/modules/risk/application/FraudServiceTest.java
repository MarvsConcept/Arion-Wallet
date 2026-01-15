package com.marv.arionwallet.modules.risk.application;

import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        User user = User.builder()
                .email("test@example.com")
                .phone("08000000000")
                .passwordHash("hashed")
                .firstName("Test")
                .lastName("User")
                .accountNumber("2999990000")
                .kycLevel(KycLevel.BASIC)
                .build();

        long todayTotal = 299_900_0000L;
        long amount = 200_000L;
    }
}
