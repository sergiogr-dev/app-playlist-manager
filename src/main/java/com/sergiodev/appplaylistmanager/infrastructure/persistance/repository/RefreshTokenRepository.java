package com.sergiodev.appplaylistmanager.infrastructure.persistance.repository;

import com.sergiodev.appplaylistmanager.domain.model.RefreshToken;
import com.sergiodev.appplaylistmanager.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    
    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByUser(User user);
    
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findActiveTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeAllTokensByUser(@Param("user") User user);
    
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeToken(@Param("token") String token);
}
