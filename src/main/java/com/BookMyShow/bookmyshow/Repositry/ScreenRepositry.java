package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ScreenRepositry extends JpaRepository<Screen,Long> {

    List<Screen> findByTheaterId(Long theaterId);
}
