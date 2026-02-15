package com.marv.arionwallet.modules.auth.domain;

import com.marv.arionwallet.modules.user.domain.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRoleRepository {

    UserRole save(UserRole userRole);
    List<UserRole> findByUserId(UUID userId);

    @Query("""
    select ur.role.name
    from UserRole ur
    where ur.user.id = :userId
    """)
    List<RoleName> findRoleNamesByUserId(UUID userId);

    boolean existsByUserIdAndRole_Name(UUID userId, RoleName roleName);

    Optional<UserRole> findByUserIdAndRole_Name(UUID userId, RoleName roleName);

    boolean existsByUserId(UUID userId);

    void delete(UserRole userRole);


}
