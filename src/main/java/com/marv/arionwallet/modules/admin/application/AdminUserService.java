package com.marv.arionwallet.modules.admin.application;

import com.marv.arionwallet.modules.audit.application.AuditService;
import com.marv.arionwallet.modules.audit.presentation.AuditAction;
import com.marv.arionwallet.modules.audit.presentation.AuditTargetType;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public void freeze(UUID actorAdminId,
                       UUID targetUserId,
                       String reason) {

        if (actorAdminId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot freeze your own account");
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        target.freeze();
        userRepository.save(target);

        auditService.record(
                actorAdminId,
                AuditAction.USER_FREEZE,
                AuditTargetType.USER,
                targetUserId.toString(),
                reason
        );
    }

    @Transactional
    public void unfreeze(UUID actorAdminId,
                         UUID targetUserId,
                         String reason) {

        User target = userRepository.findById(targetUserId).
                orElseThrow(() -> new IllegalArgumentException("User not found"));

        target.unfreeze();
        userRepository.save(target);

        auditService.record(
                actorAdminId,
                AuditAction.USER_UNFREEZE,
                AuditTargetType.USER,
                targetUserId.toString(),
                reason
        );
    }

}
