package com.BookMyShow.bookmyshow.controller;
import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Service.PaymentService;
import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.PaymentOrderResponse;
import com.BookMyShow.bookmyshow.dto.PaymentVerificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/order")
    public ResponseEntity<PaymentOrderResponse> createOrder(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Booking> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndCreateBooking(request));
    }
}
