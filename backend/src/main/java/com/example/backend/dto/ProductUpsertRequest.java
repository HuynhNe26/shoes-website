package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ProductUpsertRequest(
        String productName,
        String description,
        Map<String, Object> image,
        Map<String, Object> imageDescription,
        Boolean limited,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<VariantUpsertRequest> variants
) {
}
