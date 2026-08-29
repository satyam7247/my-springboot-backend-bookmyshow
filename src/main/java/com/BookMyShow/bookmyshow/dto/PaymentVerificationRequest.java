package com.BookMyShow.bookmyshow.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentVerificationRequest {
    @JsonAlias({"razorpay_order_id", "orderId"})
    private String razorpayOrderId;

    @JsonAlias({"razorpay_payment_id", "paymentId"})
    private String razorpayPaymentId;

    @JsonAlias({"razorpay_signature", "signature"})
    private String razorpaySignature;
}
