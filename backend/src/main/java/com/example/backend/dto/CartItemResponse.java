package com.example.backend.dto;

public record CartItemResponse(
        Long id,
        Long pvId,
        Long productId,
        String productName,
        String image,
        Integer size,
        String color,
        Integer quantity,
        Integer unitPrice,
        Integer totalPrice,
        String note,
        Boolean limited
) {
}
