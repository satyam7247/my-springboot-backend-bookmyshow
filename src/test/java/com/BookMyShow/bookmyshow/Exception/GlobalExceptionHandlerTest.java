package com.BookMyShow.bookmyshow.Exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void preservesHttpStatusFromResponseStatusException() {
        var response = new GlobalExceptionHandler().handleResponseStatusException(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody().get("message"));
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().get("status"));
    }
}
