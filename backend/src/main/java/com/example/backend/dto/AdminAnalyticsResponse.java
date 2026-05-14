package com.example.backend.dto;

import java.util.List;

public record AdminAnalyticsResponse(
        Long currentMonthRevenue,
        Long previousMonthRevenue,
        Long currentMonthOrders,
        Long forecastNextMonthRevenue,
        List<LowStockResponse> lowStock,
        String aiInsight
) {
}
