package com.BookMyShow.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor @NoArgsConstructor
public class TheaterRequest {

    private String name;
    private String address;
    private Long cityId;
}
