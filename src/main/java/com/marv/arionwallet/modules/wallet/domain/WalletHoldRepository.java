package com.marv.arionwallet.modules.wallet.domain;

import java.util.Optional;
import java.util.UUID;

public interface WalletHoldRepository {

    WalletHold save(WalletHold hold);

    Optional<WalletHold> findByTransactionId(UUID transactionId);

//    long sumActiveHoldsByWallet(UUID walletId);

    long sumActiveHoldsByWalletId(UUID walletId);
}
