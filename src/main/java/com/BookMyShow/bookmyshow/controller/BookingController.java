package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Service.BookingService;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private  final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request){
        throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Direct booking creation is disabled. Use the payment flow."
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingId(@PathVariable Long id){
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingByUserId(@PathVariable Long userId){

        return ResponseEntity.ok(bookingService.getBookingByuser(userId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id){

        return ResponseEntity.ok(bookingService.cancelBooking(id));
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
}
