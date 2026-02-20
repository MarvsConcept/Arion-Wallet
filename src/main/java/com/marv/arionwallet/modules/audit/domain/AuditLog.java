package com.marv.arionwallet.modules.audit.domain;

import com.marv.arionwallet.modules.audit.presentation.AuditAction;
import com.marv.arionwallet.modules.audit.presentation.AuditTargetType;
import com.marv.arionwallet.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_user_id", nullable = false)
    private User actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, updatable = false, length = 50)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, updatable = false, length = 50)
    private AuditTargetType targetType;

    @Column(name = "target_id", nullable = false, updatable = false, length = 100)
    private String targetId;

    // Optional metadata(reason, old/new values)
    @Column(name = "metadata", columnDefinition = "text")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public AuditLog(UUID id,
                    User actorUserId,
                    AuditAction action,
                    AuditTargetType targetType,
                    String targetId,
                    String metadata,
                    Instant createdAt) {
        Instant now = Instant.now();
        this.id = (id != null) ? id : UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.metadata = metadata;
        this.createdAt = (createdAt != null) ? createdAt : now;
    }
}
