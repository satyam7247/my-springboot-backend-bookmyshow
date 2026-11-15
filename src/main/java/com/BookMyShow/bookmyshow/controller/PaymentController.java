package com.BookMyShow.bookmyshow.controller;
import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Service.PaymentService;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.PaymentOrderResponse;
import com.BookMyShow.bookmyshow.dto.PaymentVerificationRequest;
import com.BookMyShow.bookmyshow.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/payments", "/api/payment"})
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping({"/order", "/create-order"})
    public ResponseEntity<PaymentOrderResponse> createOrder(@RequestBody BookingRequest request,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        request.setUserId(user.id());
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Booking> verifyPayment(@RequestBody PaymentVerificationRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(paymentService.verifyAndCreateBooking(request, user.id()));
    }
}
