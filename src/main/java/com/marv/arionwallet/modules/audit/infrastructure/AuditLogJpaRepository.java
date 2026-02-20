package com.marv.arionwallet.modules.audit.infrastructure;

import com.marv.arionwallet.modules.audit.domain.AuditLog;
import com.marv.arionwallet.modules.audit.domain.AuditLogRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogJpaRepository extends
        JpaRepository<AuditLog, UUID>, AuditLogRepository {

}
