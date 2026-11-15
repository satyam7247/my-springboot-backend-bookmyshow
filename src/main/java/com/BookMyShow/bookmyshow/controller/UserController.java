package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Service.UserService;
import com.BookMyShow.bookmyshow.Service.AuthSessionService;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.security.AuthenticatedUser;
import com.BookMyShow.bookmyshow.dto.AuthResponse;
import com.BookMyShow.bookmyshow.dto.LoginRequest;
import com.BookMyShow.bookmyshow.dto.UserRequest;
import com.BookMyShow.bookmyshow.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthSessionService authSessionService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRequest request) {
        UserEntity user = userService.register(request);
        return ResponseEntity.ok(AuthResponse.from(user, authSessionService.createSession(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        UserEntity user = userService.login(request);
        return ResponseEntity.ok(AuthResponse.from(user, authSessionService.createSession(user)));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false)
                                       String authorization) {
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            authSessionService.invalidate(authorization.substring(7).trim());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser().stream().map(UserResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id,
                                                    @AuthenticationPrincipal AuthenticatedUser currentUser) {
        requireSelfOrAdmin(id, currentUser);
        return ResponseEntity.ok(UserResponse.from(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
                                                      @RequestBody UserRequest request,
                                                      @AuthenticationPrincipal AuthenticatedUser currentUser) {
        requireSelfOrAdmin(id, currentUser);
        return ResponseEntity.ok(UserResponse.from(userService.updateProfile(id, request)));
    }

    private void requireSelfOrAdmin(Long id, AuthenticatedUser currentUser) {
        if (currentUser == null || (!currentUser.id().equals(id) && !"ADMIN".equalsIgnoreCase(currentUser.role()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's profile");
        }
    }
}
