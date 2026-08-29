package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepositry extends JpaRepository<UserEntity,Long> {

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
