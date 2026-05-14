package com.example.backend.controller;

import com.example.backend.dto.CheckoutRequest;
import com.example.backend.dto.OrderResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final RequestAuth requestAuth;

    @PostMapping("/checkout")
    public OrderResponse checkout(@RequestBody CheckoutRequest checkoutRequest, HttpServletRequest request) {
        return orderService.checkout(requestAuth.requireUser(request).id(), checkoutRequest);
    }

    @GetMapping
    public List<OrderResponse> myOrders(HttpServletRequest request) {
        return orderService.myOrders(requestAuth.requireUser(request).id());
    }

    @GetMapping("/{orderId}")
    public OrderResponse detail(@PathVariable Long orderId, HttpServletRequest request) {
        return orderService.detail(requestAuth.requireUser(request).id(), orderId);
    }
}
