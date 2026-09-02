package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.UserRepositry;
import com.BookMyShow.bookmyshow.dto.LoginRequest;
import com.BookMyShow.bookmyshow.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositry userRepositry;

    public UserEntity register(UserRequest request) {
        if (userRepositry.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exist" + request.getEmail());
        }
        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .role("USER")
                .phone(request.getPhone())
                .build();
        return userRepositry.save(user);
    }

    public UserEntity login(LoginRequest request) {
        UserEntity userEntity = userRepositry.findByEmail(String.valueOf(request.getEmail()))
                .orElseThrow(() -> new RuntimeException("User Not found with email : " + request.getEmail()));
        if (!userEntity.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }
        return userEntity;
    }

    public List<UserEntity> getAllUser() {
        return userRepositry.findAll();
    }

    public UserEntity getUserById(Long id) {
        return userRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("User Not found with id : " + id));
    }

    // ✅ NAYA METHOD — profile update ke liye
    public UserEntity updateProfile(Long id, UserRequest request) {
        UserEntity user = userRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id : " + id));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        return userRepositry.save(user);
    }

    public Show getShowById(Long showId) {
        return null;
    }
}
