package com.BookMyShow.bookmyshow.config;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.UserRepositry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepositry userRepositry;

    @Value("${app.admin.name:Admin}")
    private String adminName;

    @Value("${app.admin.email:admin@bookmyshow.com}")
    private String adminEmail;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepositry.existsByEmail(adminEmail)) {
            return;
        }

        UserEntity admin = UserEntity.builder()
                .name(adminName)
                .email(adminEmail)
                .password(adminPassword)
                .role("ADMIN")
                .phone("")
                .build();

        userRepositry.save(admin);
    }
}
