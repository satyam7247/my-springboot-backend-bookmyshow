package com.BookMyShow.bookmyshow.security;

public record AuthenticatedUser(Long id, String name, String email, String role) {
}
