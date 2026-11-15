package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.BookingFoodItem;
import com.BookMyShow.bookmyshow.Entity.Seat;
import com.BookMyShow.bookmyshow.Entity.Show;
import com.BookMyShow.bookmyshow.Entity.UserEntity;
import com.BookMyShow.bookmyshow.Enums.BookingStatus;
import com.BookMyShow.bookmyshow.Repositry.BookingRepositry;
import com.BookMyShow.bookmyshow.Repositry.SeatRepositry;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.FoodItemRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Map<String, FoodProduct> FOOD_MENU = Map.ofEntries(
            Map.entry("popcorn", new FoodProduct("Popcorn", 180L)),
            Map.entry("drink", new FoodProduct("Cold Drink", 120L)),
            Map.entry("nachos", new FoodProduct("Cheese Nachos", 160L)),
            Map.entry("combo", new FoodProduct("Movie Combo", 250L)),
            Map.entry("burger", new FoodProduct("Veg Burger", 130L)),
            Map.entry("hotdog", new FoodProduct("Hot Dog", 140L)),
            Map.entry("samosa", new FoodProduct("Samosa (2 pc)", 60L)),
            Map.entry("fries", new FoodProduct("French Fries", 110L)),
            Map.entry("coffee", new FoodProduct("Coffee", 80L)),
            Map.entry("icecream", new FoodProduct("Ice Cream", 90L)),
            Map.entry("water", new FoodProduct("Water Bottle", 40L))
    );

    private final BookingRepositry bookingRepositry;
    private final SeatRepositry seatRepositry;
    private final UserService userService;
    private final ShowService showService;

    private BookingContext validateAndBuildContext(BookingRequest request, boolean lockShow) {
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
        Show show = lockShow
                ? showService.getShowForBooking(request.getShowId())
                : showService.getShowById(request.getShowId());

        List<Long> requestedSeatIds = new ArrayList<>(request.getSeatIds());
        Set<Long> uniqueSeatIds = new HashSet<>(requestedSeatIds);
        if (uniqueSeatIds.size() != requestedSeatIds.size() || uniqueSeatIds.contains(null)) {
            throw new RuntimeException("Each seat may only be selected once");
        }

        Set<Long> alreadyBookedSeatIds = new HashSet<>(bookingRepositry.findBookedSeatIdsByShowId(show.getId()));

        for (Long seatId : requestedSeatIds) {
            if (seatId == null || alreadyBookedSeatIds.contains(seatId)) {
                throw new RuntimeException("Seat with id " + seatId + " is already booked");
            }
        }

        List<Seat> seats = seatRepositry.findAllById(requestedSeatIds);
        if (seats.size() != requestedSeatIds.size()) {
            throw new RuntimeException("Some seats are invalid");
        }
        boolean seatsBelongToShow = seats.stream()
                .allMatch(seat -> seat.getScreen() != null
                        && seat.getScreen().getId().equals(show.getScreen().getId()));
        if (!seatsBelongToShow) {
            throw new RuntimeException("All selected seats must belong to the show's screen");
        }

        if (show.getTicketPrice() == null) {
            throw new RuntimeException("Show ticket price is not configured");
        }
        List<BookingFoodItem> foodItems = buildFoodItems(request.getFoodItems());
        String foodDeliveryTime = validateFoodDeliveryTime(show, foodItems, request.getFoodDeliveryTime());
        double foodTotal = foodItems.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        double totalPrice = seats.size() * show.getTicketPrice() + foodTotal;

        return new BookingContext(user, show, seats, foodItems, foodDeliveryTime, totalPrice);
    }

    private List<BookingFoodItem> buildFoodItems(List<FoodItemRequest> requestedItems) {
        if (requestedItems == null || requestedItems.isEmpty()) {
            return List.of();
        }

        Set<String> selectedItemIds = new HashSet<>();
        List<BookingFoodItem> foodItems = new ArrayList<>();
        for (FoodItemRequest item : requestedItems) {
            if (item == null || item.getItemId() == null || item.getItemId().isBlank()) {
                throw new RuntimeException("Food item id is required");
            }
            if (!selectedItemIds.add(item.getItemId())) {
                throw new RuntimeException("Duplicate food item: " + item.getItemId());
            }
            if (item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 20) {
                throw new RuntimeException("Food item quantity must be between 1 and 20");
            }

            FoodProduct product = FOOD_MENU.get(item.getItemId());
            if (product == null) {
                throw new RuntimeException("Unknown food item: " + item.getItemId());
            }
            foodItems.add(new BookingFoodItem(
                    item.getItemId(),
                    product.name(),
                    item.getQuantity(),
                    product.unitPrice()
            ));
        }
        return foodItems;
    }

    private String validateFoodDeliveryTime(Show show, List<BookingFoodItem> foodItems, String requestedTime) {
        if (foodItems.isEmpty()) {
            return null;
        }
        if (requestedTime == null || requestedTime.isBlank()) {
            throw new RuntimeException("Food delivery time is required when ordering snacks");
        }

        String timeLabel = requestedTime.replaceFirst("\\s*\\(interval\\)$", "").trim();
        LocalTime deliveryTime;
        try {
            deliveryTime = LocalTime.parse(timeLabel, DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Food delivery time must be one of the available show slots");
        }

        LocalTime startTime = show.getStartTime();
        if (startTime == null) {
            throw new RuntimeException("Show start time is not configured");
        }

        int durationMinutes;
        LocalTime endTime = show.getEndTime();
        if (endTime != null && endTime.isAfter(startTime)) {
            durationMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        } else {
            Integer movieDuration = show.getMovie() == null ? null : show.getMovie().getDurationMinutes();
            durationMinutes = movieDuration == null ? 180 : movieDuration;
        }

        int startMinute = startTime.getHour() * 60 + startTime.getMinute();
        int deliveryMinute = deliveryTime.getHour() * 60 + deliveryTime.getMinute();
        int offsetMinutes = Math.floorMod(deliveryMinute - startMinute, 24 * 60);
        if (offsetMinutes >= durationMinutes || offsetMinutes % 15 != 0) {
            throw new RuntimeException("Food delivery time must be a 15-minute slot during the show");
        }
        return requestedTime.trim();
    }

    public void validateBookingRequest(BookingRequest request) {
        validateAndBuildContext(request, false);
    }

    public double calculateTotalPrice(BookingRequest request) {
        return validateAndBuildContext(request, false).totalPrice();
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        return createBooking(request, null);
    }

    @Transactional
    public Booking createBooking(BookingRequest request, String razorpayPaymentId) {
        BookingContext context = validateAndBuildContext(request, true);

        Booking booking = Booking.builder()
                .user(context.user())
                .seats(context.seats())
                .show(context.show())
                .foodItems(context.foodItems())
                .foodDeliveryTime(context.foodDeliveryTime())
                .totalPrice(context.totalPrice())
                .razorpayPaymentId(razorpayPaymentId)
                .status(BookingStatus.CONFIRMED)
                .build();

        context.foodItems().forEach(foodItem -> foodItem.setBooking(booking));
        return bookingRepositry.save(booking);
    }

    // ✅ Ye teen methods add karo
    public Booking getBookingById(Long id) {
        return bookingRepositry.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public Booking getBookingForCancellation(Long id) {
        return bookingRepositry.findByIdForUpdate(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
    }

    public List<Booking> getBookingByuser(Long userId) {
        return bookingRepositry.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepositry.findAll();
    }

    @Transactional
    public Booking cancelBooking(Long bookingId, String razorpayRefundId) {
        Booking booking = getBookingForCancellation(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        if (booking.getRazorpayPaymentId() != null
                && (razorpayRefundId == null || razorpayRefundId.isBlank())) {
            throw new IllegalStateException("A refund is required before cancelling a paid booking");
        }
        if (razorpayRefundId != null && !razorpayRefundId.isBlank()) {
            booking.setRazorpayRefundId(razorpayRefundId);
        }
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

    private record FoodProduct(String name, long unitPrice) {}
    private record BookingContext(UserEntity user, Show show, List<Seat> seats,
                                  List<BookingFoodItem> foodItems, String foodDeliveryTime, double totalPrice) {}
}
