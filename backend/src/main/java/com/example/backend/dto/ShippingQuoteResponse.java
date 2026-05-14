package com.example.backend.dto;

public record ShippingQuoteResponse(
        Double distanceKm,
        Integer transferPrice,
        String membership,
        Integer membershipDiscountPercent,
        Boolean freeShip
) {
}
