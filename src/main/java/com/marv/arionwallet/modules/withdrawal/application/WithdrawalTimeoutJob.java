package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WithdrawalTimeoutJob {

    private final TransactionRepository transactionRepository;

    @Scheduled(fixedDelay =  60_000) // 60 Seconds
    @Transactional
    public void failStalePendingWithdrawals() {
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(39));

        int updated  = transactionRepository.failStalePendingWithdrawals(cutoff);

    }
}

