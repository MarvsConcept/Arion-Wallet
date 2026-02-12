package com.marv.arionwallet.modules.auth.application;

import com.marv.arionwallet.modules.auth.domain.*;
import com.marv.arionwallet.modules.auth.infrastructure.AdminBootstrapProperties;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AdminBootstrapProperties props;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) {

        if (props.getEmail() == null || props.getEmail().isBlank()) {
            return; // no bootstrap configured
        }

        User user = userRepository.findByEmail(props.getEmail()).orElse(null);
        if (user == null ) return;

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role not seeded"));

        boolean alreadyAdmin = userRoleRepository.existsByUserIdAndRole_Name(user.getId(), RoleName.ADMIN);
        if (!alreadyAdmin) {
            userRoleRepository.save(new UserRole(user, adminRole));
        }
    }
}
