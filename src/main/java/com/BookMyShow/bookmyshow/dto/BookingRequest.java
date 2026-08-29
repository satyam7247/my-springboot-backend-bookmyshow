package com.BookMyShow.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class BookingRequest {

    private Long userId;
    private Long showId;
    private List<Long> seatIds;
}
