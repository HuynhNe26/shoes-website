package com.example.backend.controller;

import com.example.backend.dto.DeliveryConfirmRequest;
import com.example.backend.dto.DeliveryLocationMessage;
import com.example.backend.security.AuthPrincipal;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.DeliveryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final RequestAuth requestAuth;

    @PostMapping("/api/delivery/orders/{orderId}/confirm")
    public DeliveryLocationMessage confirmShipped(@PathVariable Long orderId, @RequestBody DeliveryConfirmRequest confirmRequest, HttpServletRequest request) {
        AuthPrincipal principal = requestAuth.requireAdmin(request);
        return deliveryService.confirmShipped(orderId, principal.id(), confirmRequest);
    }

    @MessageMapping("/delivery/location")
    public void deliveryLocation(DeliveryLocationMessage message) {
        deliveryService.broadcastLocation(message);
    }
}
