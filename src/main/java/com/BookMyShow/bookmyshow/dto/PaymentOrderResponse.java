package com.BookMyShow.bookmyshow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOrderResponse {
    private String keyId;
    private String orderId;
    private Long amount;
    private String currency;
    private String receipt;
    private String name;
    private String description;
}
