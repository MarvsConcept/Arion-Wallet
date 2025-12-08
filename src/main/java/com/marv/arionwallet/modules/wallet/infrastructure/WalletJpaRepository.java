package com.marv.arionwallet.modules.wallet.infrastructure;

import com.marv.arionwallet.modules.wallet.domain.Wallet;
import com.marv.arionwallet.modules.wallet.domain.WalletRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletJpaRepository
        extends JpaRepository<Wallet, UUID>, WalletRepository {

    @Override
    List<Wallet> findByUserId(UUID userId);

    @Override
    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);
}
