package com.example.backend.controller;

import com.example.backend.dto.AdminAnalyticsResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.AdminAnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {
    private final AdminAnalyticsService adminAnalyticsService;
    private final RequestAuth requestAuth;

    @GetMapping("/insights")
    public AdminAnalyticsResponse insights(HttpServletRequest request) {
        requestAuth.requireAdmin(request);
        return adminAnalyticsService.insights();
    }
}
