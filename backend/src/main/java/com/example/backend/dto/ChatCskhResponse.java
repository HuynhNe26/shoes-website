package com.example.backend.dto;

import java.time.LocalDateTime;

public record ChatCskhResponse(
        Long chatId,
        String roomId,
        Long senderId,
        String senderRole,
        String message,
        Boolean read,
        LocalDateTime createdAt
) {
}
