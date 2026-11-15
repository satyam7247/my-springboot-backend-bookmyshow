package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.UserRepositry;
import com.BookMyShow.bookmyshow.dto.LoginRequest;
import com.BookMyShow.bookmyshow.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern BCRYPT_HASH_PATTERN =
            Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepositry userRepositry;
    private final PasswordEncoder passwordEncoder;

    public UserEntity register(UserRequest request) {
        if (request == null || request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        String email = normalizeEmail(request.getEmail());
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters");
        }
        if (request.getName().length() > 100 || email.length() > 254) {
            throw new IllegalArgumentException("Name or email exceeds the allowed length");
        }
        if (request.getPhone() != null && request.getPhone().length() > 30) {
            throw new IllegalArgumentException("Phone number exceeds the allowed length");
        }
        if (request.getCity() != null && request.getCity().length() > 120) {
            throw new IllegalArgumentException("City exceeds the allowed length");
        }
        if (userRepositry.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account already exists for this email");
        }
        UserEntity user = UserEntity.builder()
                .name(request.getName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role("USER")
                .phone(request.getPhone() == null ? null : request.getPhone().trim())
                .city(trimToNull(request.getCity()))
                .build();
        return userRepositry.save(user);
    }

    public UserEntity login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String email = normalizeEmail(request.getEmail());
        UserEntity userEntity = userRepositry.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        String storedPassword = userEntity.getPassword();
        boolean legacyPlaintext = storedPassword != null && !BCRYPT_HASH_PATTERN.matcher(storedPassword).matches();
        boolean passwordMatches = legacyPlaintext
                ? Objects.equals(storedPassword, request.getPassword())
                : storedPassword != null && passwordEncoder.matches(request.getPassword(), storedPassword);
        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (legacyPlaintext) {
            userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
            userEntity = userRepositry.save(userEntity);
        }
        return userEntity;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        return email.trim().toLowerCase(java.util.Locale.ROOT);
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
        if (request == null) {
            throw new IllegalArgumentException("Profile update is required");
        }
        UserEntity user = userRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id : " + id));
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isBlank() || name.length() > 100) {
                throw new IllegalArgumentException("Name must contain 1 to 100 characters");
            }
            user.setName(name);
        }
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (phone.length() > 30) {
                throw new IllegalArgumentException("Phone number exceeds the allowed length");
            }
            user.setPhone(trimToNull(phone));
        }
        if (request.getCity() != null) {
            String city = request.getCity().trim();
            if (city.length() > 120) {
                throw new IllegalArgumentException("City exceeds the allowed length");
            }
            user.setCity(trimToNull(city));
        }
        return userRepositry.save(user);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

}
