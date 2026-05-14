package com.example.backend.dto;

import java.time.LocalDate;
import java.util.Map;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String gender,
        String address,
        LocalDate dateOfBirth,
        String phoneNumber,
        Map<String, Object> favorite
) {
}
