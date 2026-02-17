package com.marv.arionwallet.modules.kyc.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface KycProfileRepository {

    KycProfile save(KycProfile profile);
    Optional<KycProfile> findByUserId(UUID userId);
    Page<KycProfile> findByStatusOrderBySubmittedAtAsc(KycStatus status, Pageable pageable);
    boolean existsByUserId(UUID userId);
}
