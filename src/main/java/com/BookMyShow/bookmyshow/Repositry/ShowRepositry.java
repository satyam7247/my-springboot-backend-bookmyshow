package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;


public interface ShowRepositry extends JpaRepository<Show,Long> {

    List<Show> findBymovieId(Long movieId);
    List<Show> findByScreenId(Long screenId);
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    List<Show> findByScreenIdAndShowDate(Long screenId,LocalDate showDate);
}
