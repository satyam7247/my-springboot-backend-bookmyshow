package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.PaymentOrderResponse;
import com.BookMyShow.bookmyshow.dto.PaymentVerificationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String RAZORPAY_ORDERS_URL = "https://api.razorpay.com/v1/orders";
    private static final String CURRENCY = "INR";

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<String, PendingPayment> pendingPayments = new ConcurrentHashMap<>();

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @PostConstruct
    void validateConfig() {
        if (razorpayKeyId == null || razorpayKeyId.isBlank()) {
            System.out.println("Razorpay key id is not configured");
        }
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            System.out.println("Razorpay key secret is not configured");
        }
    }

    public PaymentOrderResponse createOrder(BookingRequest request) {
        bookingService.validateBookingRequest(request);

        if (razorpayKeyId == null || razorpayKeyId.isBlank()) {
            throw new RuntimeException("Razorpay key id is not configured on the backend");
        }
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new RuntimeException("Razorpay secret is not configured on the backend");
        }

        double totalPrice = bookingService.calculateTotalPrice(request);
        long amountInPaise = Math.round(totalPrice * 100.0d);
        if (amountInPaise <= 0) {
            throw new RuntimeException("Invalid payment amount");
        }

        String receipt = "booking_" + request.getUserId() + "_" + request.getShowId() + "_" + UUID.randomUUID();
        String payload = buildOrderPayload(amountInPaise, CURRENCY, receipt, request);

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(RAZORPAY_ORDERS_URL))
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", basicAuthHeader(razorpayKeyId, razorpayKeySecret))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw buildRazorpayOrderException(response);
            }

            JsonNode body = objectMapper.readTree(response.body());
            String orderId = textOrNull(body, "id");
            String currency = textOrDefault(body, "currency", CURRENCY);
            long returnedAmount = body.path("amount").asLong(amountInPaise);

            if (orderId == null || orderId.isBlank()) {
                throw new RuntimeException("Razorpay order id missing in response");
            }

            pendingPayments.put(orderId, new PendingPayment(request, returnedAmount, receipt));

            return PaymentOrderResponse.builder()
                    .keyId(razorpayKeyId)
                    .orderId(orderId)
                    .amount(returnedAmount)
                    .currency(currency)
                    .receipt(receipt)
                    .name("BookMyShow")
                    .description("Movie ticket payment")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Could not create payment order: " + e.getMessage(), e);
        }
    }

    public com.BookMyShow.bookmyshow.Entity.Booking verifyAndCreateBooking(PaymentVerificationRequest request) {
        if (request == null) {
            throw new RuntimeException("Payment verification payload is required");
        }
        if (razorpayKeyId == null || razorpayKeyId.isBlank()) {
            throw new RuntimeException("Razorpay key id is not configured on the backend");
        }
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new RuntimeException("Razorpay secret is not configured on the backend");
        }
        if (request.getRazorpayOrderId() == null || request.getRazorpayOrderId().isBlank()) {
            throw new RuntimeException("Razorpay order id is required");
        }
        if (request.getRazorpayPaymentId() == null || request.getRazorpayPaymentId().isBlank()) {
            throw new RuntimeException("Razorpay payment id is required");
        }
        if (request.getRazorpaySignature() == null || request.getRazorpaySignature().isBlank()) {
            throw new RuntimeException("Razorpay signature is required");
        }

        PendingPayment pending = pendingPayments.get(request.getRazorpayOrderId());
        if (pending == null) {
            throw new RuntimeException("No pending payment found for order id " + request.getRazorpayOrderId());
        }

        String expectedSignature = hmacSha256Hex(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                razorpayKeySecret
        );
        boolean signatureMatches = MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                request.getRazorpaySignature().getBytes(StandardCharsets.UTF_8)
        );

        if (!signatureMatches) {
            throw new RuntimeException("Payment signature verification failed");
        }

        if (pending.amountInPaise() <= 0) {
            throw new RuntimeException("Invalid pending payment amount");
        }

        try {
            com.BookMyShow.bookmyshow.Entity.Booking booking = bookingService.createBooking(pending.bookingRequest());
            pendingPayments.remove(request.getRazorpayOrderId());
            return booking;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not create booking after payment verification: " + e.getMessage(), e);
        }
    }

    private String buildOrderPayload(long amount, String currency, String receipt, BookingRequest request) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "amount", amount,
                    "currency", currency,
                    "receipt", receipt,
                    "notes", Map.of(
                            "userId", String.valueOf(request.getUserId()),
                            "showId", String.valueOf(request.getShowId()),
                            "seatCount", String.valueOf(request.getSeatIds().size())
                    )
            ));
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare Razorpay order payload", e);
        }
    }

    private String basicAuthHeader(String keyId, String keySecret) {
        String credentials = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private RuntimeException buildRazorpayOrderException(HttpResponse<String> response) {
        String body = response.body() == null ? "" : response.body();
        if (body.contains("Authentication failed")) {
            return new RuntimeException(
                    "Razorpay authentication failed. Verify that razorpay.key.id and razorpay.key.secret " +
                            "belong to the same Razorpay account and environment."
            );
        }
        return new RuntimeException("Failed to create Razorpay order (HTTP " + response.statusCode() + "): " + body);
    }

    private String hmacSha256Hex(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not verify Razorpay signature", e);
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        String value = textOrNull(node, field);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record PendingPayment(BookingRequest bookingRequest, long amountInPaise, String receipt) {}
}
