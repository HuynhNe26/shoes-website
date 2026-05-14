package com.example.backend.dto;

import java.time.LocalDateTime;

public record DeliveryLocationMessage(
        Long orderId,
        Long shipperId,
        Double latitude,
        Double longitude,
        String proofImageUrl,
        String status,
        LocalDateTime timestamp
) {
}
