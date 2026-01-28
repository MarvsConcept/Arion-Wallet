package com.marv.arionwallet.modules.withdrawal.application;

import com.marv.arionwallet.modules.transfer.presentation.TransferRequestDto;
import com.marv.arionwallet.modules.transfer.presentation.TransferResponseDto;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalRequestDto;
import com.marv.arionwallet.modules.withdrawal.presentation.WithdrawalResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawalService {

    @Transactional
    public WithdrawalResponseDto requestWithdrawal(User user,
                                                   WithdrawalRequestDto request,
                                                   String idempotencyKey) {

        // Validate Currency
        if (!request.getCurrency().trim().equalsIgnoreCase("NGN")) {
            throw new IllegalArgumentException("Only NGN is supported");
        }
    }

}


