package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductDetailResponse(
        Long productId,
        String productName,
        String description,
        Object image,
        Object imageDescription,
        Boolean limited,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<VariantResponse> variants,
        List<ProductCardResponse> recommendations
) {
}
