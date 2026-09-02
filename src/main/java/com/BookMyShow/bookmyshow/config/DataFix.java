package com.BookMyShow.bookmyshow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataFix implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS booking_seats (
                booking_id BIGINT NOT NULL,
                seat_id BIGINT NOT NULL,
                PRIMARY KEY (booking_id, seat_id),
                CONSTRAINT fk_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
                CONSTRAINT fk_seat FOREIGN KEY (seat_id) REFERENCES seats(id)
            )
        """);
        System.out.println(">>> booking_seats table check/created successfully <<<");
    }
}