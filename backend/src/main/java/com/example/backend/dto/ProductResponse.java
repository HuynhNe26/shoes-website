package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long productId,
        String productName,
        String description,
        Object image,
        Object imageDescription,
        String primaryImage,
        Boolean limited,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer minPrice,
        Integer minFinalPrice,
        Integer totalStock,
        List<VariantResponse> variants
) {
}
