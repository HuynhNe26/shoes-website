package com.example.backend.controller;

import com.example.backend.dto.ShippingQuoteRequest;
import com.example.backend.dto.ShippingQuoteResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.ShippingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;
    private final UserRepository userRepository;
    private final RequestAuth requestAuth;

    @PostMapping("/quote")
    public ShippingQuoteResponse quote(@RequestBody ShippingQuoteRequest quoteRequest, HttpServletRequest request) {
        Long userId = requestAuth.requireUser(request).id();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return shippingService.quote(quoteRequest.destinationLatitude(), quoteRequest.destinationLongitude(), user);
    }
}
