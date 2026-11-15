package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.Screen;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Repositry.BookingRepositry;
import com.BookMyShow.bookmyshow.Repositry.SeatRepositry;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.FoodItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingRepositry bookingRepositry;
    private SeatRepositry seatRepositry;
    private BookingService bookingService;
    private Screen showScreen;
    private List<Seat> seats;

    @BeforeEach
    void setUp() {
        bookingRepositry = mock(BookingRepositry.class);
        seatRepositry = mock(SeatRepositry.class);
        UserService userService = mock(UserService.class);
        ShowService showService = mock(ShowService.class);
        bookingService = new BookingService(bookingRepositry, seatRepositry, userService, showService);

        showScreen = Screen.builder().id(10L).build();
        seats = List.of(
                Seat.builder().id(1L).screen(showScreen).build(),
                Seat.builder().id(2L).screen(showScreen).build()
        );
        when(userService.getUserById(3L)).thenReturn(UserEntity.builder().id(3L).build());
        Show show = Show.builder().id(4L).screen(showScreen).ticketPrice(200.0)
                .startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(22, 0)).build();
        when(showService.getShowById(4L)).thenReturn(show);
        when(showService.getShowForBooking(4L)).thenReturn(show);
        when(bookingRepositry.findBookedSeatIdsByShowId(4L)).thenReturn(List.of());
        when(seatRepositry.findAllById(List.of(1L, 2L))).thenReturn(seats);
        when(bookingRepositry.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createBookingUsesServerFoodPricesAndStoresFoodItems() {
        Booking booking = bookingService.createBooking(bookingRequest(
                List.of(new FoodItemRequest("popcorn", 2), new FoodItemRequest("water", 1))
        ));

        assertEquals(800.0, booking.getTotalPrice());
        assertEquals(2, booking.getFoodItems().size());
        assertEquals("7:15 PM", booking.getFoodDeliveryTime());
        assertEquals(360L, booking.getFoodItems().get(0).getUnitPrice()
                * booking.getFoodItems().get(0).getQuantity());
    }

    @Test
    void createBookingRejectsSeatsFromAnotherScreen() {
        Screen otherScreen = Screen.builder().id(99L).build();
        when(seatRepositry.findAllById(List.of(1L, 2L))).thenReturn(List.of(
                seats.get(0),
                Seat.builder().id(2L).screen(otherScreen).build()
        ));

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(bookingRequest(List.of())));
    }

    @Test
    void createBookingRejectsUnknownFoodItems() {
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(
                bookingRequest(List.of(new FoodItemRequest("unknown", 1)))
        ));
    }

    @Test
    void createBookingRejectsFoodDeliveryOutsideShowTime() {
        BookingRequest request = bookingRequest(List.of(new FoodItemRequest("popcorn", 1)));
        request.setFoodDeliveryTime("10:15 PM");

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));
    }

    private BookingRequest bookingRequest(List<FoodItemRequest> foodItems) {
        BookingRequest request = new BookingRequest();
        request.setUserId(3L);
        request.setShowId(4L);
        request.setSeatIds(List.of(1L, 2L));
        request.setFoodItems(foodItems);
        request.setFoodDeliveryTime(foodItems.isEmpty() ? null : "7:15 PM");
        return request;
    }
}
