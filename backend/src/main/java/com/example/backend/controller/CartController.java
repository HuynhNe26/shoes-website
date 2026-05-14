package com.example.backend.controller;

import com.example.backend.dto.CartItemRequest;
import com.example.backend.dto.CartResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
    private final RequestAuth requestAuth;

    @GetMapping
    public CartResponse getCart(HttpServletRequest request) {
        return cartService.getCart(requestAuth.requireUser(request).id());
    }

    @PostMapping("/items")
    public CartResponse addItem(@RequestBody CartItemRequest itemRequest, HttpServletRequest request) {
        return cartService.addItem(requestAuth.requireUser(request).id(), itemRequest);
    }

    @PatchMapping("/items/{itemId}")
    public CartResponse updateItem(@PathVariable Long itemId, @RequestBody CartItemRequest itemRequest, HttpServletRequest request) {
        return cartService.updateItem(requestAuth.requireUser(request).id(), itemId, itemRequest);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long itemId, HttpServletRequest request) {
        return cartService.removeItem(requestAuth.requireUser(request).id(), itemId);
    }
}
