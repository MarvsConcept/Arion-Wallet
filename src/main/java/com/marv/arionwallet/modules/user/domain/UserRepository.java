package com.marv.arionwallet.modules.user.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByAccountNumber(String accountNumber);

    Optional<User> findByAccountNumber(String accountNumber);

    List<User> findAll();

}
