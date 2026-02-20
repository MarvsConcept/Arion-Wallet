package com.marv.arionwallet.modules.fraud.application;

import com.marv.arionwallet.modules.fraud.domain.TransferLimit;
import com.marv.arionwallet.modules.fraud.domain.WithdrawalLimit;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class FraudService {

    private final TransactionRepository transactionRepository;

    public void validateTransfer(User user, long amountInKobo) {

        // Get Users KycLevel
        KycLevel level = user.getKycLevel();

        // Get limits
        TransferLimit limit = getLimitsFor(level);

        // Check single transfer limit
        if (amountInKobo > limit.getMaxSingleInKobo()) {
            throw new IllegalArgumentException("Amount exceeded per transaction limit for your KYC level");
        }

        // Compute today's time
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Instant startInstant = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

       long todayTotal = transactionRepository.sumSuccessfulTransfersForUserBetween(
               user.getId(), startInstant, endInstant);

        // Check daily transfer limit
       if (todayTotal + amountInKobo > limit.getMaxDailyInKobo()) {
           throw new IllegalArgumentException("Daily transfer limit exceeded for your KYC level");
       }

    }

    public void validateWithdrawal(User user, long amountInKobo) {

        // Get Users KycLevel
        KycLevel level = user.getKycLevel();

        // Get Limits
        WithdrawalLimit limit = getLimitFor(level);

        // Check single withdraw limit
        if (amountInKobo > limit.getSingleWithdrawalInKobo()) {
            throw new IllegalArgumentException("Withdrawal amount exceeded per transaction for your KYC level");
        }

        // Compute today's time
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        Instant startInstant = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstant = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        long todayTotal = transactionRepository.sumNonFailedWithdrawalsForUserBetween(
                user.getId(), startInstant, endInstant);

        // Check Daily withdrawal limit
        if (todayTotal + amountInKobo > limit.getDailyWithdrawalInKobo()) {
            throw new IllegalArgumentException("Daily withdrawal limit exceeded for your KYC level");
        }

    }


    private TransferLimit getLimitsFor(KycLevel level) {

        return switch (level) {
            case NONE -> new TransferLimit(1000000,
                    500000,
                    200000
            );
            case BASIC -> new TransferLimit(30000000,
                    5000000,
                    2000000
            );
            case FULL -> new TransferLimit(500000000,
                    200000000,
                    50000000
            );
            default -> throw new IllegalArgumentException("Unknown KYC level: " + level);
        };
    }

    private WithdrawalLimit getLimitFor(KycLevel level) {

        return switch (level) {
            case NONE -> new WithdrawalLimit(5_000_000,
                    10_000_000
            );
            case BASIC -> new WithdrawalLimit(100_000_000,
                    200_000_000
            );
            case FULL -> new WithdrawalLimit(1_000_000_000,
                    2_000_000_000
            );
            default -> throw new IllegalArgumentException("Unknown KYC level: " + level);
        };
    }
}
