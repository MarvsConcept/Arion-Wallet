package com.marv.arionwallet.modules.wallet.infrastructure;

import com.marv.arionwallet.modules.wallet.domain.HoldStatus;
import com.marv.arionwallet.modules.wallet.domain.WalletHold;
import com.marv.arionwallet.modules.wallet.domain.WalletHoldRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletHoldJpaRepository
        extends JpaRepository<WalletHold, UUID>, WalletHoldRepository {

    Optional<WalletHold> findByTransactionId(UUID transactionId);

    @Query("""
           select coalesce(sum(h.amount), 0)
           from WalletHold h
           where h.walletId = :walletId and h.status = :status
           """)
    long sumByWalletIdAndStatus(@Param("walletId") UUID walletId, @Param("status") HoldStatus status);

    @Query("""
       select coalesce(sum(h.amount), 0)
       from WalletHold h
       where h.walletId = :walletId and h.status = com.marv.arionwallet.modules.wallet.domain.HoldStatus.ACTIVE
    """)
    long sumActiveHoldsByWalletId(@Param("walletId") UUID walletId);

}
