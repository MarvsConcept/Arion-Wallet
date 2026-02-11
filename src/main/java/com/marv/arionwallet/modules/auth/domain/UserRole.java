package com.marv.arionwallet.modules.auth.domain;

import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_role", columnNames = {"user_id", "role_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRole {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;


    @Builder
    public UserRole(UUID id,
                    User user,
                    Role role,
                    Instant grantedAt) {
        Instant now = Instant.now();
        this.id = (id != null) ? id : UUID.randomUUID();
        this.user = user;
        this.role = role;
        this.grantedAt = (grantedAt != null) ? grantedAt : now;
    }


    public UserRole(User user,
                    Role role) {
        this(null, user, role, null);
    }


}
