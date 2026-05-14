package com.example.backend.controller;

import com.example.backend.dto.ProductResponse;
import com.example.backend.dto.ProductUpsertRequest;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.AdminProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService adminProductService;
    private final RequestAuth requestAuth;

    @PostMapping
    public ProductResponse create(@RequestBody ProductUpsertRequest productRequest, HttpServletRequest request) {
        requestAuth.requireAdmin(request);
        return adminProductService.create(productRequest);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(@PathVariable Long productId, @RequestBody ProductUpsertRequest productRequest, HttpServletRequest request) {
        requestAuth.requireAdmin(request);
        return adminProductService.update(productId, productRequest);
    }

    @PatchMapping("/{productId}/limited")
    public ProductResponse limited(@PathVariable Long productId, @RequestBody ProductUpsertRequest productRequest, HttpServletRequest request) {
        requestAuth.requireAdmin(request);
        return adminProductService.limited(productId, productRequest);
    }

    @DeleteMapping("/{productId}")
    public void delete(@PathVariable Long productId, HttpServletRequest request) {
        requestAuth.requireAdmin(request);
        adminProductService.delete(productId);
    }
}
