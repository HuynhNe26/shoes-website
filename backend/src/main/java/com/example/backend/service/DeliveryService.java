package com.example.backend.service;

import com.example.backend.dto.DeliveryConfirmRequest;
import com.example.backend.dto.DeliveryLocationMessage;
import com.example.backend.entity.Order;
import com.example.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public DeliveryLocationMessage confirmShipped(Long orderId, Long shipperId, DeliveryConfirmRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        order.setStatus("SHIPPED");
        order.setConfirmStatus(true);
        order.setConfirmTime(LocalDateTime.now());
        order.setOrderTransaction(request.proofImageUrl());
        DeliveryLocationMessage message = new DeliveryLocationMessage(
                orderId,
                shipperId,
                request.latitude(),
                request.longitude(),
                request.proofImageUrl(),
                "SHIPPED",
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/delivery/" + orderId, message);
        return message;
    }

    public void broadcastLocation(DeliveryLocationMessage message) {
        DeliveryLocationMessage enriched = new DeliveryLocationMessage(
                message.orderId(),
                message.shipperId(),
                message.latitude(),
                message.longitude(),
                message.proofImageUrl(),
                message.status() == null ? "MOVING" : message.status(),
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/delivery/" + message.orderId(), enriched);
    }
}
