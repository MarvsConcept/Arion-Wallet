//package com.marv.arionwallet.modules.withdrawal.application;
//
//import com.marv.arionwallet.modules.transaction.domain.Transaction;
//import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
//import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
//import com.marv.arionwallet.modules.transaction.domain.TransactionType;
//import lombok.RequiredArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class WithdrawalProcessingJob {
//
//    private final TransactionRepository transactionRepository;
//    private final WithdrawalService withdrawalService;
//    private static final Logger log = LoggerFactory.getLogger(WithdrawalProcessingJob.class);
//
//    @Scheduled(fixedDelay = 60_000) // every 60 seconds
//    public void processPendingWithdrawals() {
//
//        List<Transaction> pending = transactionRepository
//                .findTop20ByTypeAndStatusOrderByCreatedAtAsc(
//                        TransactionType.WITHDRAWAL,
//                        TransactionStatus.PENDING
//                );
//
//        for (Transaction tx : pending) {
//            try {
//                withdrawalService.processWithdrawal(tx.getReference());
//            } catch (Exception e) {
//                log.error("Failed processing withdrawal {}", tx.getReference(), e);
//            }
//        }
//    }
//}
