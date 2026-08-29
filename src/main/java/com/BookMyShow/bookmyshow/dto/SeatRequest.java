package com.BookMyShow.bookmyshow.dto;

import com.BookMyShow.bookmyshow.Enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class SeatRequest {

    private String seatNumber;
    private String row;
    private Integer col;
    private SeatType seatType;
    private Long screenId;
}
