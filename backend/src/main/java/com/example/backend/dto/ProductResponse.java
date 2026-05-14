package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProductResponse(
        Long productId,
        String productName,
        String description,
        Map<String, Object> image,
        Map<String, Object> imageDescription,
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
