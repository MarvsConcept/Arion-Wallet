package com.marv.arionwallet.modules.wallet.application;

import com.marv.arionwallet.modules.wallet.domain.HoldStatus;
import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletHold;
import com.marv.arionwallet.modules.wallet.domain.WalletHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HoldService {

    private final WalletHoldRepository walletHoldRepository;

    public long availableBalance(Wallet wallet) {
        long activeHolds = walletHoldRepository.sumActiveHoldsByWalletId(wallet.getId());
        return wallet.getBalance() - activeHolds;
    }

    public WalletHold createActiveHold(Wallet wallet,
                                       UUID transactionId,
                                       long amount,
                                       String currency) {

        WalletHold hold = WalletHold.builder()
                .walletId(wallet.getId())
                .transactionId(transactionId)
                .amount(amount)
                .currency(currency)
                .status(HoldStatus.ACTIVE)
                .build();

        return walletHoldRepository.save(hold);
    }

    public void releaseHold(UUID transactionId) {
        WalletHold hold = walletHoldRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalStateException("Hold missing for transaction " + transactionId));
    }

    public void consumeHold(UUID transactionId) {
        WalletHold hold = walletHoldRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalStateException("Hold missing for transaction " + transactionId));
        if (hold.getStatus() == HoldStatus.ACTIVE) {
            hold.consume();
            walletHoldRepository.save(hold);
        }
    }

    public void requireActiveHold(UUID transactionId) {
        WalletHold hold = walletHoldRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new IllegalStateException("Hold missing for transaction " + transactionId));
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Hold is not ACTIVE for transaction " + transactionId);
        }
    }


}
