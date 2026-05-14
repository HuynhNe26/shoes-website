package com.example.backend.dto;

public record VariantResponse(
        Long variantId,
        Integer price,
        Integer priceDiscount,
        Integer finalPrice,
        Long sizeId,
        Integer size,
        Long colorId,
        String color,
        String image,
        Integer stock,
        Integer soldQuantity,
        Boolean status
) {
    public VariantResponse(
            Long variantId,
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
        this(variantId, price, priceDiscount, finalPrice, null, size, null, color, image, stock, soldQuantity, status);
    }
}
