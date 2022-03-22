package com.minerva.MinervaDatabase.database.repository;

import com.minerva.MinervaDatabase.database.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Override
    Optional<RefreshToken> findById(Long aLong);

    Optional<RefreshToken> findByUserId(Long id);
}
