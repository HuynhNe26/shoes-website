package com.example.backend.dto;

public record AuthResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String role,
        String membership,
        Integer point
) {
}
