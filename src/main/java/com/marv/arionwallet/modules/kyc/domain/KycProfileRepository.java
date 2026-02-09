package com.marv.arionwallet.modules.kyc.domain;

import java.util.Optional;
import java.util.UUID;

public interface KycProfileRepository {

    KycProfile save(KycProfile profile);
    Optional<KycProfile> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
