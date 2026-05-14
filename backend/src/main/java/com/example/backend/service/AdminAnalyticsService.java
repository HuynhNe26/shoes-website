package com.example.backend.service;

import com.example.backend.dto.AdminAnalyticsResponse;
import com.example.backend.dto.LowStockResponse;
import com.example.backend.entity.ProductVariant;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final GeminiService geminiService;

    @Transactional(readOnly = true)
    public AdminAnalyticsResponse insights() {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        LocalDateTime previousMonthStart = monthStart.minusMonths(1);
        long currentRevenue = orderRepository.sumRevenueBetween(monthStart, nextMonthStart);
        long previousRevenue = orderRepository.sumRevenueBetween(previousMonthStart, monthStart);
        long currentOrders = orderRepository.countByCreatedAtBetween(monthStart, nextMonthStart);
        long forecast = forecast(currentRevenue, previousRevenue, today.getDayOfMonth(), today.lengthOfMonth());
        List<LowStockResponse> lowStock = productVariantRepository.findLowStock(5).stream()
                .map(this::toLowStock)
                .toList();
        String fallback = buildFallbackInsight(currentRevenue, previousRevenue, forecast, lowStock);
        String prompt = """
                Ban la AI phan tich ecommerce giay. Dua ra nhan xet ngan gon cho admin bang tieng Viet.
                Doanh thu thang nay: %d
                Doanh thu thang truoc: %d
                Du bao thang sau: %d
                So don thang nay: %d
                Ton kho thap: %s
                """.formatted(currentRevenue, previousRevenue, forecast, currentOrders, lowStock);
        String insight = geminiService.generate(prompt, fallback);
        return new AdminAnalyticsResponse(currentRevenue, previousRevenue, currentOrders, forecast, lowStock, insight);
    }

    private long forecast(long currentRevenue, long previousRevenue, int currentDay, int daysInMonth) {
        long runRate = currentDay <= 0 ? currentRevenue : Math.round(currentRevenue * (daysInMonth / (double) currentDay));
        return Math.round((runRate * 0.7) + (previousRevenue * 0.3));
    }

    private LowStockResponse toLowStock(ProductVariant variant) {
        return new LowStockResponse(
                variant.getPvId(),
                variant.getProduct().getProductId(),
                variant.getProduct().getProductName(),
                variant.getSize() == null ? null : variant.getSize().getValue(),
                variant.getColor() == null ? null : variant.getColor().getColor(),
                variant.getStock()
        );
    }

    private String buildFallbackInsight(long currentRevenue, long previousRevenue, long forecast, List<LowStockResponse> lowStock) {
        String trend = currentRevenue >= previousRevenue ? "Doanh thu dang tang so voi thang truoc." : "Doanh thu dang giam so voi thang truoc.";
        String stock = lowStock.isEmpty()
                ? "Chua co bien the nao duoi nguong ton kho."
                : "Can uu tien nhap them " + lowStock.size() + " bien the sap het hang.";
        return trend + " Du bao thang sau khoang " + forecast + " VND. " + stock;
    }
}
