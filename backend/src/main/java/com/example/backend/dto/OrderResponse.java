package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long orderId,
        String orderCode,
        String status,
        LocalDateTime createdAt,
        Integer subTotal,
        Integer voucherDiscount,
        Integer membershipDiscount,
        Integer taxPrice,
        Integer transferPrice,
        Integer totalPrice,
        List<OrderItemResponse> items
) {
}
