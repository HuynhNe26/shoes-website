package com.example.backend.dto;

public record CategoryResponse(
        Long categoryId,
        String categoryName,
        Boolean status
) {
}
