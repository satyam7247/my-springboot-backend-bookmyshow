package com.BookMyShow.bookmyshow.Entity;


import com.BookMyShow.bookmyshow.Enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private UserEntity user;


    @ManyToOne
    @JoinColumn(name = "show_id",nullable = false)
    private Show show;


    @ManyToMany
    @JoinTable(name = "booking_seats",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "seat_id"))
    private List<Seat> seats;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingFoodItem> foodItems;

    private String foodDeliveryTime;

    private  Double totalPrice;

    @Column(name = "razorpay_payment_id", unique = true)
    private String razorpayPaymentId;

    @Column(name = "razorpay_refund_id", unique = true)
    private String razorpayRefundId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime bookedAt;

    @PrePersist
    private void onCreate(){
        this.bookedAt=LocalDateTime.now();
        if (this.status == null) {
            this.status = BookingStatus.CONFIRMED;
        }
    }

}
