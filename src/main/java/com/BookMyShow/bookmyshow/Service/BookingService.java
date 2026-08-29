package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Enums.BookingStatus;
import com.BookMyShow.bookmyshow.Repositry.BookingRepositry;
import com.BookMyShow.bookmyshow.Repositry.SeatRepositry;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepositry bookingRepositry;
    private final SeatRepositry seatRepositry;
    private final UserService userService;
    private final ShowService showService;

    private BookingContext validateAndBuildContext(BookingRequest request) {
        if (request == null) {
            throw new RuntimeException("Booking request is required");
        }
        if (request.getUserId() == null) {
            throw new RuntimeException("User id is required");
        }
        if (request.getShowId() == null) {
            throw new RuntimeException("Show id is required");
        }
        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new RuntimeException("At least one seat must be selected");
        }

        UserEntity user = userService.getUserById(request.getUserId());
        Show show = showService.getShowById(request.getShowId());

        List<Long> alreadyBookedSeatIds = bookingRepositry.findBookedSeatIdsByShowId(show.getId());
        List<Long> requestedSeatIds = new ArrayList<>(request.getSeatIds());

        for (Long seatId : requestedSeatIds) {
            if (alreadyBookedSeatIds.contains(seatId)) {
                throw new RuntimeException("Seat with id " + seatId + " is already booked");
            }
        }

        List<Seat> seats = seatRepositry.findAllById(requestedSeatIds);
        if (seats.size() != requestedSeatIds.size()) {
            throw new RuntimeException("Some seats are invalid");
        }

        if (show.getTicketPrice() == null) {
            throw new RuntimeException("Show ticket price is not configured");
        }
        double totalPrice = seats.size() * show.getTicketPrice();

        return new BookingContext(user, show, seats, totalPrice);
    }

    public void validateBookingRequest(BookingRequest request) {
        validateAndBuildContext(request);
    }

    public double calculateTotalPrice(BookingRequest request) {
        return validateAndBuildContext(request).totalPrice();
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        BookingContext context = validateAndBuildContext(request);

        Booking booking = Booking.builder()
                .user(context.user())
                .seats(context.seats())
                .show(context.show())
                .totalPrice(context.totalPrice())
                .status(BookingStatus.CONFIRMED)
                .build();

        return bookingRepositry.save(booking);
    }

    // ✅ Ye teen methods add karo
    public Booking getBookingById(Long id) {
        return bookingRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingByuser(Long userId) {
        return bookingRepositry.findByUserId(userId);
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepositry.save(booking);
    }

    public List<Seat> getAvaliableSeat(Long showId) {
        Show show = showService.getShowById(showId);
        List<Seat> allSeats = seatRepositry.findByScreenId(show.getScreen().getId());
        List<Long> bookedSeatIds = bookingRepositry.findBookedSeatIdsByShowId(showId);
        return allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()))
                .toList();
    }

    private record BookingContext(UserEntity user, Show show, List<Seat> seats, double totalPrice) {}
}
