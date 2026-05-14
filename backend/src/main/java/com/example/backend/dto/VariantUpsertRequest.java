package com.example.backend.dto;

public record VariantUpsertRequest(
        Long pvId,
        Integer price,
        Integer priceDiscount,
        Long sizeId,
        Long colorId,
        String image,
        Integer stock,
        Boolean status
) {
}
