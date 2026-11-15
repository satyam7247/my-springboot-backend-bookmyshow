package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.AuthSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AuthSessionRepositry extends JpaRepository<AuthSession, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<AuthSession> findByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    void deleteByTokenHash(String tokenHash);

    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}
