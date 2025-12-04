package com.marv.arionwallet.modules.user.infrastructure;

import com.marv.arionwallet.modules.user.domain.User;
import com.marv.arionwallet.modules.user.domain.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository
        extends JpaRepository<User, UUID>, UserRepository {

    @Override
    Optional<User> findByEmail(String email);

    @Override
    Optional<User> findByPhone(String phone);

    @Override
    boolean existsByEmail(String email);

    @Override
    boolean existsByPhone(String phone);
}

