package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.UserRepositry;
import com.BookMyShow.bookmyshow.dto.LoginRequest;
import com.BookMyShow.bookmyshow.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void registrationHashesThePassword() {
        UserRepositry userRepositry = mock(UserRepositry.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserService userService = new UserService(userRepositry, passwordEncoder);
        when(userRepositry.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(userRepositry.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserEntity user = userService.register(new UserRequest(
                "Test User", "user@example.com", "plain-password", "5551234567", null
        ));

        assertNotEquals("plain-password", user.getPassword());
        assertTrue(passwordEncoder.matches("plain-password", user.getPassword()));
    }

    @Test
    void loginUpgradesAnExistingPlaintextPassword() {
        UserRepositry userRepositry = mock(UserRepositry.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserService userService = new UserService(userRepositry, passwordEncoder);
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("user@example.com")
                .password("legacy-password")
                .build();
        when(userRepositry.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(userRepositry.save(user)).thenReturn(user);

        UserEntity loggedInUser = userService.login(new LoginRequest("user@example.com", "legacy-password"));

        assertNotEquals("legacy-password", loggedInUser.getPassword());
        assertTrue(passwordEncoder.matches("legacy-password", loggedInUser.getPassword()));
        verify(userRepositry).save(user);
    }

    @Test
    void profileUpdatePersistsTrimmedCity() {
        UserRepositry userRepositry = mock(UserRepositry.class);
        UserService userService = new UserService(userRepositry, new BCryptPasswordEncoder());
        UserEntity user = UserEntity.builder().id(1L).name("Test User").build();
        when(userRepositry.findById(1L)).thenReturn(Optional.of(user));
        when(userRepositry.save(user)).thenReturn(user);

        UserEntity updated = userService.updateProfile(1L,
                new UserRequest("Updated User", null, null, "5551234567", "  Delhi  "));

        assertEquals("Updated User", updated.getName());
        assertEquals("Delhi", updated.getCity());
        verify(userRepositry).save(user);
    }
}
