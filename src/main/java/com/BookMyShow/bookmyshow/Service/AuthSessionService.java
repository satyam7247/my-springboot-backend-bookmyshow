package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.AuthSession;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.AuthSessionRepositry;
import com.BookMyShow.bookmyshow.security.AuthenticatedUser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthSessionService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long SESSION_HOURS = 12;

    private final AuthSessionRepositry authSessionRepositry;

    public AuthSessionService(AuthSessionRepositry authSessionRepositry) {
        this.authSessionRepositry = authSessionRepositry;
    }

    @Transactional
    public String createSession(UserEntity user) {
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        LocalDateTime now = LocalDateTime.now();

        authSessionRepositry.deleteByExpiresAtBefore(now);
        authSessionRepositry.save(AuthSession.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .expiresAt(now.plusHours(SESSION_HOURS))
                .build());
        return token;
    }

    @Transactional
    public AuthenticatedUser authenticate(String token) {
        return authSessionRepositry.findByTokenHashAndExpiresAtAfter(hashToken(token), LocalDateTime.now())
                .map(session -> {
                    UserEntity user = session.getUser();
                    return new AuthenticatedUser(user.getId(), user.getName(), user.getEmail(), user.getRole());
                })
                .orElse(null);
    }

    @Transactional
    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            authSessionRepositry.deleteByTokenHash(hashToken(token));
        }
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash access token", e);
        }
    }
}
