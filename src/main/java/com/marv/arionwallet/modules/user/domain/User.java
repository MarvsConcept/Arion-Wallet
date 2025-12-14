package com.marv.arionwallet.modules.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "account_number", unique = true, nullable = false, updatable = false, length = 10)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", nullable = false)
    private KycLevel kycLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder
    private User(UUID id,
                 String email,
                 String phone,
                 String passwordHash,
                 String firstName,
                 String lastName,
                 String accountNumber,
                 UserStatus status,
                 KycLevel kycLevel,
                 Instant createdAt,
                 Instant updatedAt) {
        Instant now = Instant.now();

        this.id = (id != null ) ? id : UUID.randomUUID();
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountNumber = accountNumber;
        this.status = (status != null) ? status : UserStatus.ACTIVE;
        this.kycLevel = (kycLevel != null) ? kycLevel : KycLevel.NONE;
        this.createdAt = (createdAt != null) ? createdAt : now;
        this.updatedAt = (updatedAt != null) ? updatedAt : now;

    }

    //Domain methods

    public void freeze() {
        this.status = UserStatus.FROZEN;
        this.updatedAt = Instant.now();
    }

    public void unfreeze() {
        this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void upgradeKyc(KycLevel newLevel) {
        if (newLevel.ordinal() > this.kycLevel.ordinal()) {
            this.kycLevel = newLevel;
            this.updatedAt = Instant.now();
        }
    }

}
