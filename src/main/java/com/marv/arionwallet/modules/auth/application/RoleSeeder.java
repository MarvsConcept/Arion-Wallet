package com.marv.arionwallet.modules.auth.application;

import com.marv.arionwallet.modules.auth.domain.Role;
import com.marv.arionwallet.modules.auth.domain.RoleName;
import com.marv.arionwallet.modules.auth.domain.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.hibernate.engine.internal.Versioning.seed;

@Component
@RequiredArgsConstructor
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seed(RoleName.USER, "Default role for all users");
        seed(RoleName.ADMIN, "System administrator");
        seed(RoleName.COMPLIANCE, "Compliance officer");
    }

    private void seed(RoleName name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(new Role(name, description));
        }
    }
}


