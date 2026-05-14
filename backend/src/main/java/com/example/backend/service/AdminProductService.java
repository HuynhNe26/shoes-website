package com.example.backend.service;

import com.example.backend.dto.ProductResponse;
import com.example.backend.dto.ProductUpsertRequest;
import com.example.backend.dto.VariantUpsertRequest;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductVariant;
import com.example.backend.repository.ProductColorRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ProductVariantRepository;
import com.example.backend.repository.ShoeSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AdminProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ShoeSizeRepository shoeSizeRepository;
    private final ProductColorRepository productColorRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponse create(ProductUpsertRequest request) {
        Product product = new Product();
        applyProduct(product, request);
        Product saved = productRepository.save(product);
        upsertVariants(saved, request);
        saved.setVariants(productVariantRepository.findByProduct(saved));
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long productId, ProductUpsertRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        applyProduct(product, request);
        upsertVariants(product, request);
        Product saved = productRepository.save(product);
        saved.setVariants(productVariantRepository.findByProduct(saved));
        return productMapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse limited(Long productId, ProductUpsertRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        product.setLimited(request.limited());
        product.setStartTime(request.startTime());
        product.setEndTime(request.endTime());
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        productVariantRepository.findByProduct(product).forEach(variant -> variant.setStatus(false));
    }

    private void applyProduct(Product product, ProductUpsertRequest request) {
        product.setProductName(request.productName());
        product.setDescription(request.description());
        product.setImage(request.image());
        product.setImageDescription(request.imageDescription());
        product.setLimited(Boolean.TRUE.equals(request.limited()));
        product.setStartTime(request.startTime());
        product.setEndTime(request.endTime());
    }

    private void upsertVariants(Product product, ProductUpsertRequest request) {
        if (request.variants() == null) {
            return;
        }
        for (VariantUpsertRequest variantRequest : request.variants()) {
            ProductVariant variant = variantRequest.pvId() == null
                    ? new ProductVariant()
                    : productVariantRepository.findById(variantRequest.pvId())
                    .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product variant not found"));
            variant.setProduct(product);
            variant.setPrice(variantRequest.price());
            variant.setPriceDiscount(variantRequest.priceDiscount());
            variant.setImage(variantRequest.image());
            variant.setStock(variantRequest.stock());
            variant.setStatus(variantRequest.status());
            if (variantRequest.sizeId() != null) {
                variant.setSize(shoeSizeRepository.findById(variantRequest.sizeId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Size not found")));
            }
            if (variantRequest.colorId() != null) {
                variant.setColor(productColorRepository.findById(variantRequest.colorId())
                        .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Color not found")));
            }
            productVariantRepository.save(variant);
        }
    }
}
