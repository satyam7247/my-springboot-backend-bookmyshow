package com.BookMyShow.bookmyshow.controller;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Service.BookingService;
import com.BookMyShow.bookmyshow.Service.PaymentService;
import com.BookMyShow.bookmyshow.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingControllerTest {

    private BookingService bookingService;
    private PaymentService paymentService;
    private BookingController bookingController;
    private Booking booking;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        paymentService = mock(PaymentService.class);
        bookingController = new BookingController(bookingService, paymentService);
        booking = Booking.builder()
                .id(20L)
                .user(UserEntity.builder().id(10L).build())
                .build();
        when(bookingService.getBookingById(20L)).thenReturn(booking);
    }

    @Test
    void bookingOwnerCanReadBooking() {
        assertEquals(booking, bookingController.getBookingId(
                20L, new AuthenticatedUser(10L, "Owner", "owner@example.com", "USER")
        ).getBody());
    }

    @Test
    void anotherUserCannotReadBooking() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingController.getBookingId(
                        20L, new AuthenticatedUser(11L, "Other", "other@example.com", "USER")
                ));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void nonAdminCannotListEveryBooking() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingController.getAllBookings(
                        new AuthenticatedUser(10L, "Owner", "owner@example.com", "USER")
                ));

        assertEquals(403, exception.getStatusCode().value());
        verify(bookingService, never()).getAllBookings();
    }

    @Test
    void adminCanListEveryBooking() {
        when(bookingService.getAllBookings()).thenReturn(List.of(booking));

        assertEquals(List.of(booking), bookingController.getAllBookings(
                new AuthenticatedUser(1L, "Admin", "admin@example.com", "ADMIN")
        ).getBody());
    }

    @Test
    void anotherUserCannotCancelBooking() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bookingController.cancelBooking(
                        20L, new AuthenticatedUser(11L, "Other", "other@example.com", "USER")
                ));

        assertEquals(403, exception.getStatusCode().value());
        verify(paymentService, never()).cancelBooking(20L);
    }
}
