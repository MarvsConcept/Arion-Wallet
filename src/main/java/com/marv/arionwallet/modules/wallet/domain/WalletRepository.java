package com.marv.arionwallet.modules.wallet.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(UUID id);

    List<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
}
