package com.BookMyShow.bookmyshow.Entity;

import com.BookMyShow.bookmyshow.Enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String seatNumber;

    @Column(name = "seat_row")
    private String row;

    @Column(name = "seat_col")
    private Integer col;


    @Enumerated(EnumType.STRING) // enum ki value string me le li he
    private SeatType seatType;


    @ManyToOne
    @JoinColumn(name = "screen_id",nullable = false)
    private Screen screen;

}
