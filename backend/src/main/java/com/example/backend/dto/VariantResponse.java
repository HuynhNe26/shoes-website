package com.example.backend.dto;

public record VariantResponse(
        Long pvId,
        Integer price,
        Integer priceDiscount,
        Integer finalPrice,
        Integer size,
        String color,
        String image,
        Integer stock,
        Integer soldQuantity,
        Boolean status
) {
}
