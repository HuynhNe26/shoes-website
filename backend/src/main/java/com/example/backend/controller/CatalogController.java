package com.example.backend.controller;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.dto.ProductCardResponse;
import com.example.backend.dto.ProductDetailResponse;
import com.example.backend.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;

    @GetMapping("/products")
    public List<ProductCardResponse> products(@RequestParam(required = false) String keyword) {
        return catalogService.list(keyword);
    }

    @GetMapping("/products/{productId}")
    public ProductDetailResponse detail(@PathVariable Long productId) {
        return catalogService.detail(productId);
    }

    @GetMapping("/products/sale")
    public List<ProductCardResponse> sale(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return catalogService.sale(date);
    }

    @GetMapping("/products/limited")
    public List<ProductCardResponse> limited() {
        return catalogService.limited();
    }

    @GetMapping("/products/best-sellers")
    public List<ProductCardResponse> bestSellers(@RequestParam(defaultValue = "12") int size) {
        return catalogService.bestSellers(size);
    }

    @GetMapping("/products/recommendations")
    public List<ProductCardResponse> recommendations(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String interests,
            @RequestParam(defaultValue = "12") int size
    ) {
        return catalogService.recommend(gender, interests, size);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() {
        return catalogService.categories();
    }
}
