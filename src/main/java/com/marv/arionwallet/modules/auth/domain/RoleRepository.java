package com.marv.arionwallet.modules.auth.domain;

import java.util.Optional;

public interface RoleRepository {

    Role save(Role role);
    Optional<Role> findByName(RoleName name);
    boolean existsByName(RoleName name);
}
