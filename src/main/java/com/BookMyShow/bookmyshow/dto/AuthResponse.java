package com.BookMyShow.bookmyshow.dto;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final String role;
    private final String phone;
    private final String city;
    private final LocalDateTime createAt;
    private final String accessToken;
    private final String tokenType;

    public static AuthResponse from(UserEntity user, String accessToken) {
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getCity(),
                user.getCreateAt(),
                accessToken,
                "Bearer"
        );
    }
}
