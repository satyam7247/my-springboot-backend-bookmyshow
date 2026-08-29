package com.BookMyShow.bookmyshow.Entity;


import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Integer totalSeats;

    @ManyToOne
    @JoinColumn(name = "theater_id",nullable = false)
    private Theater theater;
}
