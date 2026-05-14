package com.example.backend.service;

import com.example.backend.dto.CategoryResponse;
import com.example.backend.dto.ProductCardResponse;
import com.example.backend.dto.ProductDetailResponse;
import com.example.backend.dto.VariantResponse;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductVariant;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CatalogService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductCardResponse> list(String keyword) {
        String normalized = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<Product> products = normalized == null
                ? productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : productRepository.searchProducts(normalized);
        return products.stream()
                .map(this::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse detail(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        List<VariantResponse> variants = productVariantRepository.findByProduct_ProductId(productId).stream()
                .map(this::toVariant)
                .toList();
        List<ProductCardResponse> recommendations = recommend(null, product.getProductName(), 8).stream()
                .filter(card -> !Objects.equals(card.productId(), productId))
                .limit(6)
                .toList();
        return new ProductDetailResponse(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getImage(),
                product.getImageDescription(),
                product.getLimited(),
                product.getStartTime(),
                product.getEndTime(),
                variants,
                recommendations
        );
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> sale(LocalDate date) {
        LocalDateTime eventTime = date == null ? LocalDateTime.now() : date.atStartOfDay();
        return productRepository.findSaleProducts().stream()
                .filter(product -> product.getStartTime() == null || product.getEndTime() == null
                        || !Boolean.TRUE.equals(product.getLimited())
                        || (!eventTime.isBefore(product.getStartTime()) && !eventTime.isAfter(product.getEndTime())))
                .map(this::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> limited() {
        return productRepository.findActiveLimited(LocalDateTime.now()).stream()
                .map(this::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> bestSellers(int size) {
        return productRepository.findBestSellers(PageRequest.of(0, Math.max(1, Math.min(size, 24)))).stream()
                .map(this::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductCardResponse> recommend(String gender, String interests, int size) {
        int max = Math.max(1, Math.min(size, 24));
        LinkedHashMap<Long, Product> ordered = new LinkedHashMap<>();
        addSearch(ordered, gender);
        if (StringUtils.hasText(interests)) {
            Arrays.stream(interests.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(term -> addSearch(ordered, term));
        }
        productRepository.findBestSellers(PageRequest.of(0, max)).forEach(product -> ordered.putIfAbsent(product.getProductId(), product));
        return ordered.values().stream()
                .limit(max)
                .map(this::toCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .map(category -> new CategoryResponse(category.getCategoryId(), category.getCategoryName(), category.getStatus()))
                .toList();
    }

    ProductCardResponse toCard(Product product) {
        List<ProductVariant> variants = productVariantRepository.findByProduct_ProductId(product.getProductId());
        Integer minPrice = variants.stream()
                .map(ProductVariant::getPrice)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(null);
        Integer salePrice = variants.stream()
                .map(ProductVariant::getPriceDiscount)
                .filter(price -> price != null && price > 0)
                .min(Integer::compareTo)
                .orElse(null);
        Integer stock = variants.stream().map(ProductVariant::getStock).filter(Objects::nonNull).reduce(0, Integer::sum);
        Integer sold = variants.stream().map(ProductVariant::getSoldQuantity).filter(Objects::nonNull).reduce(0, Integer::sum);
        List<String> colors = variants.stream()
                .map(ProductVariant::getColor)
                .filter(Objects::nonNull)
                .map(color -> color.getColor())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Integer> sizes = variants.stream()
                .map(ProductVariant::getSize)
                .filter(Objects::nonNull)
                .map(size -> size.getValue())
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        String displayImage = variants.stream()
                .map(ProductVariant::getImage)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(firstImageValue(product.getImage()));
        return new ProductCardResponse(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getImage(),
                displayImage,
                product.getLimited(),
                product.getStartTime(),
                product.getEndTime(),
                minPrice,
                salePrice,
                stock,
                sold,
                colors,
                sizes
        );
    }

    VariantResponse toVariant(ProductVariant variant) {
        return new VariantResponse(
                variant.getPvId(),
                variant.getPrice(),
                variant.getPriceDiscount(),
                finalPrice(variant),
                variant.getSize() == null ? null : variant.getSize().getSizeId(),
                variant.getSize() == null ? null : variant.getSize().getValue(),
                variant.getColor() == null ? null : variant.getColor().getColorId(),
                variant.getColor() == null ? null : variant.getColor().getColor(),
                variant.getImage(),
                variant.getStock(),
                variant.getSoldQuantity(),
                variant.getStatus()
        );
    }

    private Integer finalPrice(ProductVariant variant) {
        Integer price = variant.getPrice() == null ? 0 : variant.getPrice();
        Integer discount = variant.getPriceDiscount();
        if (discount != null && discount > 0 && discount < price) {
            return discount;
        }
        return price;
    }

    private void addSearch(LinkedHashMap<Long, Product> ordered, String term) {
        if (!StringUtils.hasText(term)) {
            return;
        }
        productRepository.searchProducts(term.trim()).forEach(product -> ordered.putIfAbsent(product.getProductId(), product));
    }

    private String firstImageValue(Object image) {
        if (image == null) {
            return null;
        }
        if (image instanceof Map<?, ?> map) {
            return map.values().stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);
        }
        if (image instanceof Iterable<?> iterable) {
            for (Object value : iterable) {
                if (value != null && StringUtils.hasText(value.toString())) {
                    return value.toString();
                }
            }
            return null;
        }
        return Optional.of(image)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(StringUtils::hasText)
                .orElse(null);
    }
}
