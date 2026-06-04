package com.bitlord.authservice.repository;

import com.bitlord.authservice.model.RefreshToken;
import com.bitlord.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for RefreshToken entity.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
