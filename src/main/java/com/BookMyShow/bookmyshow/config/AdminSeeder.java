package com.BookMyShow.bookmyshow.config;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.UserRepositry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepositry userRepositry;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.name:Admin}")
    private String adminName;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            LOGGER.warn("Admin seeding skipped because ADMIN_EMAIL or ADMIN_PASSWORD is not configured");
            return;
        }
        if (userRepositry.existsByEmailIgnoreCase(adminEmail.trim())) {
            return;
        }

        UserEntity admin = UserEntity.builder()
                .name(adminName.trim())
                .email(adminEmail.trim().toLowerCase(java.util.Locale.ROOT))
                .password(passwordEncoder.encode(adminPassword))
                .role("ADMIN")
                .phone("")
                .build();

        userRepositry.save(admin);
    }
}
