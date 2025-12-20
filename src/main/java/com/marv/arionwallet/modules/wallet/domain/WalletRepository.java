package com.marv.arionwallet.modules.wallet.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository {

    Wallet save(Wallet wallet);

    Optional<Wallet> findById(UUID id);

    List<Wallet> findByUserId(UUID userId);

    Optional<Wallet> findByUserIdAndCurrency(UUID userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.user.id = :userId AND w.currency = :currency")
    Optional<Wallet> findByUserIdAndCurrencyForUpdate(UUID userId, String currency);
}
