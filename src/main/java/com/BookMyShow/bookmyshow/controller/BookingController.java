package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Service.BookingService;
import com.BookMyShow.bookmyshow.Service.PaymentService;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private  final BookingService bookingService;
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request){
        throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Direct booking creation is disabled. Use the payment flow."
        );
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings(@AuthenticationPrincipal AuthenticatedUser user) {
        requireAdmin(user);
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingId(@PathVariable Long id,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        Booking booking = bookingService.getBookingById(id);
        requireOwnerOrAdmin(booking, user);
        return ResponseEntity.ok(booking);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingByUserId(@PathVariable Long userId,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null || (!isAdmin(user) && !Objects.equals(user.id(), userId))) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "You may only view your own bookings");
        }
        return ResponseEntity.ok(bookingService.getBookingByuser(userId));
    }

    @RequestMapping(value = "/{id}/cancel", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        Booking booking = bookingService.getBookingById(id);
        requireOwnerOrAdmin(booking, user);
        return ResponseEntity.ok(paymentService.cancelBooking(id));
    }

    @GetMapping("/available-seats")
    public ResponseEntity<List<Seat>> getAvailableSeatsByQuery(@RequestParam Long showId) {
        return ResponseEntity.ok(bookingService.getAvaliableSeat(showId));
    }

    @GetMapping("/available-seats/{showId}")
    public ResponseEntity<List<Seat>> getAvailableSeatsByPath(@PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getAvaliableSeat(showId));
    }

    @GetMapping("/show/{showId}/available-seats")
    public ResponseEntity<List<Seat>> getAvailableSeatsByNestedPath(@PathVariable Long showId) {
        return ResponseEntity.ok(bookingService.getAvaliableSeat(showId));
    }

    private void requireOwnerOrAdmin(Booking booking, AuthenticatedUser user) {
        if (user == null || booking.getUser() == null
                || (!isAdmin(user) && !Objects.equals(booking.getUser().getId(), user.id()))) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "You may only access your own bookings");
        }
    }

    private boolean isAdmin(AuthenticatedUser user) {
        return user != null && user.role() != null && user.role().equalsIgnoreCase("ADMIN");
    }

    private void requireAdmin(AuthenticatedUser user) {
        if (!isAdmin(user)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN,
                    "Administrator access is required");
        }
    }
}
