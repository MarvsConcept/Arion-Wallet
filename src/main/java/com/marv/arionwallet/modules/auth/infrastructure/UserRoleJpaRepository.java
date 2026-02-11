package com.marv.arionwallet.modules.auth.infrastructure;

import com.marv.arionwallet.modules.auth.domain.UserRole;
import com.marv.arionwallet.modules.auth.domain.UserRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRoleJpaRepository extends JpaRepository<UserRole, UUID>, UserRoleRepository {


}
