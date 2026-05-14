package com.example.backend.service;

import com.example.backend.dto.ProductResponse;
import com.example.backend.dto.VariantResponse;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductVariant;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        List<VariantResponse> variants = product.getVariants().stream()
                .map(this::toVariantResponse)
                .toList();

        Integer minPrice = product.getVariants().stream()
                .map(ProductVariant::getPrice)
                .filter(price -> price != null && price > 0)
                .min(Integer::compareTo)
                .orElse(null);
        Integer minFinalPrice = product.getVariants().stream()
                .map(this::finalPrice)
                .filter(price -> price != null && price > 0)
                .min(Integer::compareTo)
                .orElse(null);
        Integer totalStock = product.getVariants().stream()
                .map(ProductVariant::getStock)
                .filter(stock -> stock != null && stock > 0)
                .reduce(0, Integer::sum);

        return new ProductResponse(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getImage(),
                product.getImageDescription(),
                primaryImage(product),
                product.getLimited(),
                product.getStartTime(),
                product.getEndTime(),
                minPrice,
                minFinalPrice,
                totalStock,
                variants
        );
    }

    public VariantResponse toVariantResponse(ProductVariant variant) {
        return new VariantResponse(
                variant.getPvId(),
                variant.getPrice(),
                variant.getPriceDiscount(),
                finalPrice(variant),
                variant.getSize() == null ? null : variant.getSize().getValue(),
                variant.getColor() == null ? null : variant.getColor().getColor(),
                variant.getImage(),
                variant.getStock(),
                variant.getSoldQuantity(),
                variant.getStatus()
        );
    }

    public int finalPrice(ProductVariant variant) {
        Integer price = variant.getPrice() == null ? 0 : variant.getPrice();
        Integer discount = variant.getPriceDiscount();
        if (discount != null && discount > 0 && discount < price) {
            return discount;
        }
        return price;
    }

    private String primaryImage(Product product) {
        String variantImage = product.getVariants().stream()
                .map(ProductVariant::getImage)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
        if (variantImage != null) {
            return variantImage;
        }
        Object image = product.getImage();
        if (image == null) {
            return null;
        }
        if (image instanceof Map<?, ?> map) {
            return map.values().stream()
                    .flatMap(value -> {
                        if (value instanceof Iterable<?> iterable) {
                            return toStringList(iterable).stream();
                        }
                        return List.of(value.toString()).stream();
                    })
                    .filter(value -> value != null && !value.isBlank())
                    .min(Comparator.comparingInt(String::length))
                    .orElse(null);
        }
        if (image instanceof Iterable<?> iterable) {
            return toStringList(iterable).stream()
                    .filter(value -> value != null && !value.isBlank())
                    .min(Comparator.comparingInt(String::length))
                    .orElse(null);
        }
        return image.toString().isBlank() ? null : image.toString();
    }

    private List<String> toStringList(Iterable<?> values) {
        return java.util.stream.StreamSupport.stream(values.spliterator(), false)
                .map(value -> value == null ? null : value.toString())
                .toList();
    }
}
