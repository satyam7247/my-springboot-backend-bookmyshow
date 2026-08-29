package com.BookMyShow.bookmyshow.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter // lombok laga he
@NoArgsConstructor @AllArgsConstructor // lombol dependance lagi he
@Builder // object ko easy tarike se create karta he koi order matter nahi karta
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false , unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    private String phone;
    private LocalDateTime createAt;

    @PrePersist
    protected void Oncreate(){
        this.createAt=LocalDateTime.now();
        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }
    }

}
