package com.marv.arionwallet.modules.risk.application;

import com.marv.arionwallet.modules.risk.domain.TransferLimit;
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

        // Compute todays time
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
}
