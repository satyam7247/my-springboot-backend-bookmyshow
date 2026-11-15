package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.Show;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ShowRepositry extends JpaRepository<Show,Long> {

    List<Show> findBymovieId(Long movieId);
    List<Show> findByScreenId(Long screenId);
    List<Show> findByMovieIdAndShowDate(Long movieId, LocalDate showDate);
    List<Show> findByScreenIdAndShowDate(Long screenId,LocalDate showDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Show s where s.id = :showId")
    Optional<Show> findByIdForBooking(@Param("showId") Long showId);
}
