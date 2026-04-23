package com.booknest.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.booknest.auth.entity.PasswordResetToken;

// Repository for password reset token persistence
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // Find a reset token entity by its string value
    Optional<PasswordResetToken> findByToken(String token);
}