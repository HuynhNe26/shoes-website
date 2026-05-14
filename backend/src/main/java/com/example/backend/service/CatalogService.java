package com.example.backend.service;

import com.example.backend.dto.ProductResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String keyword) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return productRepository.searchProducts(normalized).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse detail(Long productId) {
        return productRepository.findById(productId)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> saleToday() {
        return productRepository.findSaleProducts().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> limitedNow() {
        return productRepository.findActiveLimited(LocalDateTime.now()).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> recommendations(Optional<AuthPrincipal> principal, String gender, String interest) {
        String keyword = Stream.of(interest, gender, favoriteKeyword(principal))
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
        List<ProductResponse> byPreference = search(keyword);
        if (!byPreference.isEmpty()) {
            return byPreference.stream().limit(12).toList();
        }
        return productRepository.findBestSellers(PageRequest.of(0, 12)).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    private String favoriteKeyword(Optional<AuthPrincipal> principal) {
        if (principal.isEmpty() || !principal.get().isUser()) {
            return null;
        }
        return userRepository.findById(principal.get().id())
                .map(User::getFavorite)
                .map(this::firstKeyword)
                .orElse(null);
    }

    private String firstKeyword(Map<String, Object> favorite) {
        if (favorite == null || favorite.isEmpty()) {
            return null;
        }
        return favorite.values().stream()
                .flatMap(this::flatten)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private Stream<String> flatten(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(item -> item == null ? null : item.toString());
        }
        return Stream.of(value == null ? null : value.toString());
    }
}
