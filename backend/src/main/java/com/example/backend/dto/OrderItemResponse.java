package com.example.backend.dto;

public record OrderItemResponse(
        Long pvId,
        String productName,
        Integer size,
        String color,
        Integer quantity,
        Integer priceAtPurchase,
        Integer totalPrice
) {
}
