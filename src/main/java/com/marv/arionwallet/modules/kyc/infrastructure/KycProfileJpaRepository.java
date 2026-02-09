package com.marv.arionwallet.modules.kyc.infrastructure;

import com.marv.arionwallet.modules.kyc.domain.KycProfile;
import com.marv.arionwallet.modules.kyc.domain.KycProfileRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KycProfileJpaRepository
        extends JpaRepository<KycProfile, UUID>, KycProfileRepository {
}
