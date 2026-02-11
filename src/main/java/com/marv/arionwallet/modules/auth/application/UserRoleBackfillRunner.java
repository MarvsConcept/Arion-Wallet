package com.marv.arionwallet.modules.auth.application;

import com.marv.arionwallet.modules.auth.domain.*;
import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class UserRoleBackfillRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) {

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("USER role not seeded"));

        for (User user : userRepository.findAll()) {
            if (!userRoleRepository.existsByUserId(user.getId())) {
                userRoleRepository.save(new UserRole(user, userRole));
            }
        }
    }
}
