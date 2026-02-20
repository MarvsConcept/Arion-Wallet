package com.marv.arionwallet.modules.audit.application;

import com.marv.arionwallet.modules.audit.domain.AuditLog;
import com.marv.arionwallet.modules.audit.domain.AuditLogRepository;
import com.marv.arionwallet.modules.audit.presentation.AuditAction;
import com.marv.arionwallet.modules.audit.presentation.AuditTargetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(UUID actorUserId,
                       AuditAction action,
                       AuditTargetType targetType,
                       String targetId,
                       String metadata) {

        AuditLog log = AuditLog.builder()
                .actorUserId(actorUserId)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .metadata(metadata)
                .build();

        auditLogRepository.save(log);
    }
}
