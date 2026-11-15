package com.BookMyShow.bookmyshow.Service;

import com.BookMyShow.bookmyshow.dto.BookingRequest;
import com.BookMyShow.bookmyshow.dto.PaymentOrderResponse;
import com.BookMyShow.bookmyshow.dto.PaymentVerificationRequest;
import com.BookMyShow.bookmyshow.Entity.Booking;
import com.BookMyShow.bookmyshow.Entity.PaymentIntent;
import com.BookMyShow.bookmyshow.Enums.BookingStatus;
import com.BookMyShow.bookmyshow.Repositry.PaymentIntentRepositry;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String DEFAULT_RAZORPAY_API_BASE_URL = "https://api.razorpay.com/v1";
    private static final String CURRENCY = "INR";

    private final BookingService bookingService;
    private final PaymentIntentRepositry paymentIntentRepositry;
    private final JsonMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${razorpay.key.id:}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Value("${razorpay.api.base-url:" + DEFAULT_RAZORPAY_API_BASE_URL + "}")
    private String razorpayApiBaseUrl;

    @Value("${booking.time-zone:Asia/Kolkata}")
    private String bookingTimeZone = "Asia/Kolkata";

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
        paymentIntentRepositry.deleteByCreatedAtBefore(java.time.LocalDateTime.now().minusDays(1));
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
                    .uri(URI.create(razorpayApiBaseUrl + "/orders"))
                    .timeout(Duration.ofSeconds(20))
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

            paymentIntentRepositry.save(PaymentIntent.builder()
                    .orderId(orderId)
                    .bookingRequestJson(objectMapper.writeValueAsString(request))
                    .amountInPaise(returnedAmount)
                    .receipt(receipt)
                    .build());

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

    @Transactional
    public com.BookMyShow.bookmyshow.Entity.Booking verifyAndCreateBooking(
            PaymentVerificationRequest request, Long authenticatedUserId) {
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

        PaymentIntent pending = paymentIntentRepositry.findById(request.getRazorpayOrderId()).orElse(null);
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

        if (pending.getAmountInPaise() == null || pending.getAmountInPaise() <= 0) {
            throw new RuntimeException("Invalid pending payment amount");
        }

        try {
            BookingRequest bookingRequest = objectMapper.readValue(pending.getBookingRequestJson(), BookingRequest.class);
            if (!authenticatedUserId.equals(bookingRequest.getUserId())) {
                throw new RuntimeException("Payment order does not belong to the authenticated user");
            }
            long recalculatedAmount = Math.round(bookingService.calculateTotalPrice(bookingRequest) * 100.0d);
            if (recalculatedAmount != pending.getAmountInPaise()) {
                throw new RuntimeException("Booking amount changed after the payment order was created");
            }
            verifyAndCapturePayment(
                    request.getRazorpayPaymentId(),
                    request.getRazorpayOrderId(),
                    pending.getAmountInPaise()
            );
            com.BookMyShow.bookmyshow.Entity.Booking booking =
                    bookingService.createBooking(bookingRequest, request.getRazorpayPaymentId());
            paymentIntentRepositry.delete(pending);
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
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            return new RuntimeException(
                    "Razorpay authentication failed. Verify that razorpay.key.id and razorpay.key.secret " +
                            "belong to the same Razorpay account and environment."
            );
        }
        return new RuntimeException("Failed to create Razorpay order (HTTP " + response.statusCode() + ")");
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
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Could not verify Razorpay signature", e);
        }
    }

    private void verifyAndCapturePayment(String paymentId, String orderId, long expectedAmount) throws Exception {
        String encodedPaymentId = URLEncoder.encode(paymentId, StandardCharsets.UTF_8);
        HttpResponse<String> paymentResponse = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(razorpayApiBaseUrl + "/payments/" + encodedPaymentId))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", basicAuthHeader(razorpayKeyId, razorpayKeySecret))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        if (paymentResponse.statusCode() < 200 || paymentResponse.statusCode() >= 300) {
            throw new RuntimeException("Could not verify payment status with Razorpay");
        }

        JsonNode payment = objectMapper.readTree(paymentResponse.body());
        if (!orderId.equals(textOrNull(payment, "order_id"))
                || payment.path("amount").asLong(-1) != expectedAmount
                || !CURRENCY.equals(textOrNull(payment, "currency"))) {
            throw new RuntimeException("Payment details do not match the created order");
        }

        String status = textOrDefault(payment, "status", "");
        if ("authorized".equals(status)) {
            String capturePayload = objectMapper.writeValueAsString(Map.of(
                    "amount", expectedAmount,
                    "currency", CURRENCY
            ));
            HttpResponse<String> captureResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(razorpayApiBaseUrl + "/payments/" + encodedPaymentId + "/capture"))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .header("Authorization", basicAuthHeader(razorpayKeyId, razorpayKeySecret))
                            .POST(HttpRequest.BodyPublishers.ofString(capturePayload))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (captureResponse.statusCode() < 200 || captureResponse.statusCode() >= 300) {
                throw new RuntimeException("Razorpay did not capture the authorized payment");
            }
            payment = objectMapper.readTree(captureResponse.body());
            status = textOrDefault(payment, "status", "");
        }
        if (!"captured".equals(status)) {
            throw new RuntimeException("Payment has not been captured");
        }
    }

    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingService.getBookingForCancellation(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }

        if (booking.getShow() == null || booking.getShow().getShowDate() == null
                || booking.getShow().getStartTime() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Show start time is unavailable; booking cannot be cancelled");
        }
        LocalDateTime showStart = LocalDateTime.of(
                booking.getShow().getShowDate(),
                booking.getShow().getStartTime()
        );
        if (!LocalDateTime.now(ZoneId.of(bookingTimeZone)).isBefore(showStart)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Bookings can only be cancelled before the show starts");
        }

        String refundId = booking.getRazorpayRefundId();
        if (booking.getRazorpayPaymentId() != null && refundId == null) {
            refundId = refundCapturedPayment(booking);
        }
        return bookingService.cancelBooking(bookingId, refundId);
    }

    private String refundCapturedPayment(Booking booking) {
        if (razorpayKeyId == null || razorpayKeyId.isBlank()
                || razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Razorpay credentials are required to refund a paid booking");
        }
        if (booking.getTotalPrice() == null || !Double.isFinite(booking.getTotalPrice())
                || booking.getTotalPrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking amount is invalid; refund cannot be created");
        }

        long expectedAmount = Math.round(booking.getTotalPrice() * 100.0d);
        if (expectedAmount <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Booking amount is invalid; refund cannot be created");
        }

        String encodedPaymentId = URLEncoder.encode(booking.getRazorpayPaymentId(), StandardCharsets.UTF_8);
        String paymentUrl = razorpayApiBaseUrl + "/payments/" + encodedPaymentId;
        String refundsUrl = paymentUrl + "/refunds";
        long alreadyRefunded = 0;
        String existingRefundId = null;
        try {
            HttpResponse<String> refundsResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(refundsUrl))
                            .timeout(Duration.ofSeconds(20))
                            .header("Authorization", basicAuthHeader(razorpayKeyId, razorpayKeySecret))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (refundsResponse.statusCode() < 200 || refundsResponse.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Could not check existing Razorpay refunds");
            }

            JsonNode refunds = objectMapper.readTree(refundsResponse.body()).path("items");
            for (int i = 0; i < refunds.size(); i++) {
                JsonNode refund = refunds.get(i);
                String status = textOrDefault(refund, "status", "");
                if (!"pending".equals(status) && !"processed".equals(status)) {
                    continue;
                }
                long amount = refund.path("amount").asLong(0);
                if (amount > 0) {
                    alreadyRefunded += amount;
                    existingRefundId = textOrNull(refund, "id");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Interrupted while checking existing Razorpay refunds", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not check existing Razorpay refunds", e);
        }

        if (alreadyRefunded == expectedAmount) {
            if (existingRefundId != null) {
                return existingRefundId;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Existing Razorpay refunds could not be reconciled");
        }
        if (alreadyRefunded > expectedAmount) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Existing Razorpay refunds exceed the booking amount");
        }

        long refundAmount = expectedAmount - alreadyRefunded;
        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of("amount", refundAmount));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not prepare Razorpay refund request", e);
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(paymentUrl + "/refund"))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .header("Authorization", basicAuthHeader(razorpayKeyId, razorpayKeySecret))
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Razorpay could not create the booking refund");
            }
            JsonNode refund = objectMapper.readTree(response.body());
            String refundId = textOrNull(refund, "id");
            String status = textOrDefault(refund, "status", "");
            if (refundId == null || !refund.path("amount").isNumber()
                    || refund.path("amount").asLong() != refundAmount
                    || (!"pending".equals(status) && !"processed".equals(status))) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Razorpay did not confirm the requested refund");
            }
            return refundId;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Interrupted while creating the Razorpay refund", e);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not create the Razorpay refund", e);
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
}
