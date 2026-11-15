package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Enums.BookingStatus;
import com.BookMyShow.bookmyshow.Repositry.PaymentIntentRepositry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceCancellationTest {

    private BookingService bookingService;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);
        paymentService = new PaymentService(
                bookingService,
                mock(PaymentIntentRepositry.class),
                JsonMapper.builder().build()
        );
    }

    @Test
    void paidBookingIsNotCancelledWhenRefundCredentialsAreMissing() {
        Booking booking = bookingWithShow(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1));
        booking.setRazorpayPaymentId("pay_test");
        when(bookingService.getBookingForCancellation(4L)).thenReturn(booking);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paymentService.cancelBooking(4L));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        verify(bookingService, never()).cancelBooking(4L, null);
    }

    @Test
    void bookingCannotBeCancelledAfterShowtime() {
        Booking booking = bookingWithShow(LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1));
        when(bookingService.getBookingForCancellation(4L)).thenReturn(booking);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> paymentService.cancelBooking(4L));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(bookingService, never()).cancelBooking(4L, null);
    }

    @Test
    void unpaidBookingCanBeCancelledBeforeShowtime() {
        Booking booking = bookingWithShow(LocalDate.now(ZoneId.of("Asia/Kolkata")).plusDays(1));
        when(bookingService.getBookingForCancellation(4L)).thenReturn(booking);
        when(bookingService.cancelBooking(4L, null)).thenAnswer(invocation -> {
            booking.setStatus(BookingStatus.CANCELLED);
            return booking;
        });

        assertEquals(BookingStatus.CANCELLED, paymentService.cancelBooking(4L).getStatus());
    }

    private Booking bookingWithShow(LocalDate date) {
        return Booking.builder()
                .id(4L)
                .totalPrice(500.0)
                .show(Show.builder()
                        .showDate(date)
                        .startTime(LocalTime.of(20, 0))
                        .build())
                .build();
    }
}
