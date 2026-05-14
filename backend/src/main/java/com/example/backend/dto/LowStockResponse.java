package com.example.backend.dto;

public record LowStockResponse(
        Long pvId,
        Long productId,
        String productName,
        Integer size,
        String color,
        Integer stock
) {
}
