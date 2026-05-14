package com.example.backend.dto;

import java.time.LocalDateTime;

public record VoucherRequest(
        String voucherCode,
        Boolean voucherType,
        Integer voucherDiscount,
        Integer minOrderValue,
        Integer maxReductionValue,
        Integer quantity,
        String description,
        String contributor,
        String contributorImage,
        LocalDateTime voucherStart,
        LocalDateTime voucherEnd,
        Boolean status,
        Boolean notifyVipDiamond
) {
}
