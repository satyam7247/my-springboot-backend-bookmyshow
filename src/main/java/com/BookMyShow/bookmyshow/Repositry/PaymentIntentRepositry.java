package com.BookMyShow.bookmyshow.Repositry;

import com.BookMyShow.bookmyshow.Entity.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PaymentIntentRepositry extends JpaRepository<PaymentIntent, String> {
    void deleteByCreatedAtBefore(LocalDateTime cutoff);
}
