package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Service.UserService;
import com.BookMyShow.bookmyshow.dto.LoginRequest;
import com.BookMyShow.bookmyshow.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateProfile(@PathVariable Long id,
                                                    @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateProfile(id, request));
    }
}
