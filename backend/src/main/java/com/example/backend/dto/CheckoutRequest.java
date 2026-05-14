package com.example.backend.dto;

public record CheckoutRequest(
        String voucherCode,
        Double destinationLatitude,
        Double destinationLongitude,
        String note
) {
}
