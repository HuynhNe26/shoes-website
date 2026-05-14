package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductCardResponse(
        Long productId,
        String productName,
        String description,
        Object image,
        String displayImage,
        Boolean limited,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer minPrice,
        Integer salePrice,
        Integer stock,
        Integer soldQuantity,
        List<String> colors,
        List<Integer> sizes
) {
}
