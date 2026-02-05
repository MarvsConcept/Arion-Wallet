package com.marv.arionwallet.modules.kyc.domain;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "kyc_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KycProfile {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KycStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private KycLevel level;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at", updatable = false)
    private Instant reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Builder
    public KycProfile(UUID id,
                      User user,
                      KycStatus status,
                      KycLevel level,
                      Instant submittedAt,
                      Instant reviewedAt,
                      String rejectionReason) {
        Instant now = Instant.now();
        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.status = status;
        this.level = level != null ? level : KycLevel.NONE;
        this.submittedAt = submittedAt != null ? submittedAt : now;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }

    public void submit() {
        if (this.status == KycStatus.APPROVED) {
            throw new IllegalStateException("Approved KYC cannot be resubmitted");
        }
        this.status = KycStatus.PENDING;
        this.level = KycLevel.NONE;
        this.rejectionReason = null;
        this.submittedAt = Instant.now();
        this.reviewedAt = null;
    }

    public void approve(KycLevel level) {
        if (this.status != KycStatus.PENDING) {
            throw new IllegalStateException("Only pending KYC can be approved");
        }
        if (level == null || level == KycLevel.NONE) {
            throw new IllegalStateException("Approved KYC must have a level");
        }
        this.status = KycStatus.APPROVED;
        this.level = level;
        this.reviewedAt = Instant.now();
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (this.status != KycStatus.PENDING) {
            throw new IllegalStateException("Only pending KYC can be rejected");
        }

        this.status = KycStatus.REJECTED;
        this.level = KycLevel.NONE;
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason;
    }

    public boolean isApproved() {
        return status == KycStatus.APPROVED;
    }


}
