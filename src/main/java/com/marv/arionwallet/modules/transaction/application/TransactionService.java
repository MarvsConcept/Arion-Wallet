package com.marv.arionwallet.modules.transaction.application;

import com.marv.arionwallet.modules.transaction.domain.Transaction;
import com.marv.arionwallet.modules.transaction.domain.TransactionRepository;
import com.marv.arionwallet.modules.transaction.domain.TransactionStatus;
import com.marv.arionwallet.modules.transaction.domain.TransactionType;
import com.marv.arionwallet.modules.transaction.presentation.TransactionHistoryItemDto;
import com.marv.arionwallet.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<TransactionHistoryItemDto> getUserTransactions(User user,
                                                               TransactionType type,
                                                               TransactionStatus status,
                                                               int page, int size) {

        // Create a pageable
        Pageable pageable = PageRequest.of(page, size);

        // Calls the repository for the current user and ordered
        Page<Transaction> txPage;


        if (type != null && status != null) {
            // If type and status is provided, filter by type and status
            txPage = transactionRepository.findByUserIdAndTypeAndStatusOrderByCreatedAtDesc(
                    user.getId(), type, status, pageable);
        } else if (type != null) {
            // If only type is provided, filter by status
            txPage = transactionRepository.findByUserIdAndTypeOrderByCreatedAtDesc(
                    user.getId(), type, pageable);
        } else if (status != null) {
            // If only status is provided, filter by status
            txPage = transactionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                    user.getId(), status, pageable);
        } else {
            // If there's no filter, return all
            txPage = transactionRepository.findByUserIdOrderByCreatedAtDesc(
                    user.getId(), pageable);
        }

        return  txPage.map(tx ->
                TransactionHistoryItemDto.builder()
                        .reference(tx.getReference())
                        .type(tx.getType())
                        .status(tx.getStatus())
                        .amountInKobo(tx.getAmount())
                        .currency(tx.getCurrency())
                        .description(tx.getDescription())
                        .createdAt(tx.getCreatedAt())
                        .build());

    }
}
