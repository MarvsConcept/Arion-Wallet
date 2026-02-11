package com.marv.arionwallet.modules.auth.infrastructure;

import com.marv.arionwallet.modules.auth.domain.Role;
import com.marv.arionwallet.modules.auth.domain.RoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<Role, UUID>, RoleRepository {

}
