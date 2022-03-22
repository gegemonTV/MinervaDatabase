package com.minerva.MinervaDatabase.database.repository;

import com.minerva.MinervaDatabase.database.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    Optional<User> findById(Long aLong);

    Optional<User> findByAlias(String alias);

    Optional<List<User>> findAllByUsername(String username);

    Optional<User> findByResetPasswordToken(String token);

    Optional<User> findByPhone(String phone);

    Boolean existsByAlias(String alias);

    Boolean existsByPhone(String phone);
}
