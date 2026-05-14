package com.example.backend.controller;

import com.example.backend.dto.ProductResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.CatalogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogService catalogService;
    private final RequestAuth requestAuth;

    @GetMapping
    public List<ProductResponse> search(@RequestParam(required = false) String keyword) {
        return catalogService.search(keyword);
    }

    @GetMapping("/{productId}")
    public ProductResponse detail(@PathVariable Long productId) {
        return catalogService.detail(productId);
    }

    @GetMapping("/sale-today")
    public List<ProductResponse> saleToday() {
        return catalogService.saleToday();
    }

    @GetMapping("/limited-now")
    public List<ProductResponse> limitedNow() {
        return catalogService.limitedNow();
    }

    @GetMapping("/recommendations")
    public List<ProductResponse> recommendations(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String interest,
            HttpServletRequest request
    ) {
        return catalogService.recommendations(requestAuth.optional(request), gender, interest);
    }
}
