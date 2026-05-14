package com.example.backend.dto;

public record CartItemRequest(Long pvId, Integer quantity, String note) {
}
