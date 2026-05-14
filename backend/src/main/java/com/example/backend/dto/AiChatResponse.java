package com.example.backend.dto;

import java.util.List;

public record AiChatResponse(
        String sessionId,
        String answer,
        String intent,
        Boolean canAddToCart,
        Long pvId,
        Integer quantity,
        String cskhRoomId,
        List<ProductCardResponse> suggestedProducts
) {
}
