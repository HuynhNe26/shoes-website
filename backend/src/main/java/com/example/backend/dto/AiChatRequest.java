package com.example.backend.dto;

public record AiChatRequest(String sessionId, String message, Long pvId, Integer quantity) {
}
