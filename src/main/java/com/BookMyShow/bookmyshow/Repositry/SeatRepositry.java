package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface SeatRepositry extends JpaRepository<Seat,Long> {

    List<Seat> findByScreenId(Long screenId);
}
