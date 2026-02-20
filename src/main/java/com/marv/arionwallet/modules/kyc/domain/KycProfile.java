package com.marv.arionwallet.modules.kyc.domain;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
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

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Past
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "id_type", nullable = false)
    private String idType;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private KycStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private KycLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_level", nullable = false)
    private KycLevel requestedLevel;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Builder
    public KycProfile(UUID id,
                      User user,
                      String fullName,
                      LocalDate dateOfBirth,
                      String address,
                      String idType,
                      String idNumber,
                      KycStatus status,
                      KycLevel level,
                      KycLevel requestedLevel,
                      Instant submittedAt,
                      Instant reviewedAt,
                      String rejectionReason) {
        Instant now = Instant.now();
        this.id = id != null ? id : UUID.randomUUID();
        this.user = user;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.idType = idType;
        this.idNumber = idNumber;
        this.status = status;
        this.level = level != null ? level : KycLevel.NONE;
        this.requestedLevel = requestedLevel != null ? requestedLevel : KycLevel.NONE;
        this.submittedAt = submittedAt != null ? submittedAt : now;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }



//    public void submit() {
//        if (this.status == KycStatus.APPROVED) {
//            throw new IllegalStateException("Approved KYC cannot be resubmitted");
//        }
//        this.status = KycStatus.PENDING;
//        this.level = KycLevel.NONE;
//        this.rejectionReason = null;
//        this.submittedAt = Instant.now();
//        this.reviewedAt = null;
//    }
//

    public void submitWithDetails(String fullName,
                                  LocalDate dateOfBirth,
                                  String address,
                                  String idType,
                                  String idNumber,
                                  KycLevel requestedLevel) {

        if (this.status == KycStatus.PENDING) {
            throw new IllegalStateException("KYC submission already pending review");
        }

        if (requestedLevel == null || requestedLevel == KycLevel.NONE) {
            throw new IllegalArgumentException("Requested KYC level must be BASIC or FULL");
        }

        if (this.status == KycStatus.APPROVED && requestedLevel.ordinal() <= this.level.ordinal()) {
            throw new IllegalStateException("You can only request an upgrade above your current approved level");
        }

        // update details
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
        this.idType = idType;
        this.idNumber = idNumber;

        // open a new review request
        this.status = KycStatus.PENDING;
        this.requestedLevel = requestedLevel;
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
        this.requestedLevel = KycLevel.NONE;
        this.reviewedAt = Instant.now();
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (this.status != KycStatus.PENDING) {
            throw new IllegalStateException("Only pending KYC can be rejected");
        }

        this.status = KycStatus.REJECTED;
        this.reviewedAt = Instant.now();
        this.rejectionReason = reason;
    }

    public boolean isApproved() {
        return status == KycStatus.APPROVED;
    }

}
