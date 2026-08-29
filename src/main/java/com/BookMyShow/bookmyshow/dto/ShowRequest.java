package com.BookMyShow.bookmyshow.dto;

import com.BookMyShow.bookmyshow.Enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class ShowRequest {

    private Long movieId;
    private Long screenId;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double ticketPrice;
}
