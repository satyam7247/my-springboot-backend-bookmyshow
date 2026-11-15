package com.BookMyShow.bookmyshow.Repositry;
import com.BookMyShow.bookmyshow.Entity.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepositry extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);
    List<Booking> findByShowId(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId")
    Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);

    @Query("SELECT s.id FROM Booking b JOIN b.seats s WHERE b.show.id=:showId AND b.status='CONFIRMED'")
    List<Long> findBookedSeatIdsByShowId(@Param("showId") Long showId);
}