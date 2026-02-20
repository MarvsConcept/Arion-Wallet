package com.marv.arionwallet.modules.policy.application;

import com.marv.arionwallet.modules.user.domain.KycLevel;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserStatus;
import org.springframework.stereotype.Service;

@Service
public class AccessPolicyService {

    public void requireActive(User user) {

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Account is not Active");
        }
    }

    public void requireKycAtLeast(User user, KycLevel minimum) {

        if (user.getKycLevel().ordinal() < minimum.ordinal()) {
            throw new IllegalStateException("KYC level too low for this action");
        }
    }
}
