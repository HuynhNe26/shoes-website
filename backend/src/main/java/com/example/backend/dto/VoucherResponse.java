package com.example.backend.dto;

import java.time.LocalDateTime;

public record VoucherResponse(
        Long voucherId,
        String voucherCode,
        Boolean voucherType,
        Integer voucherDiscount,
        Integer minOrderValue,
        Integer maxReductionValue,
        Integer quantity,
        Integer usedQuantity,
        String description,
        String contributor,
        String contributorImage,
        LocalDateTime voucherStart,
        LocalDateTime voucherEnd,
        Boolean status
) {
}
