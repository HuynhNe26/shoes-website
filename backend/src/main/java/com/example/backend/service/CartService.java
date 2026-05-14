package com.example.backend.service;

import com.example.backend.dto.CartItemRequest;
import com.example.backend.dto.CartItemResponse;
import com.example.backend.dto.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.ProductVariant;
import com.example.backend.entity.User;
import com.example.backend.repository.CartItemRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductVariantRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CartService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        User user = user(userId);
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return new CartResponse(null, List.of(), 0);
        }
        return toResponse(cart, cartItemRepository.findByCart_CartId(cart.getCartId()));
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity must be greater than 0");
        }
        User user = user(userId);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart created = new Cart();
            created.setUser(user);
            return cartRepository.save(created);
        });
        ProductVariant variant = variant(request.pvId());

        CartItem item = cartItemRepository.findByCartAndProductVariant(cart, variant).orElseGet(() -> {
            CartItem created = new CartItem();
            created.setCart(cart);
            created.setProductVariant(variant);
            created.setQuantity(0);
            return created;
        });

        int newQuantity = item.getQuantity() + request.quantity();
        validateQuantity(variant, newQuantity);
        item.setQuantity(newQuantity);
        item.setNote(request.note());
        item.setTotalPrice(productMapper.finalPrice(variant) * newQuantity);
        cartItemRepository.save(item);
        return toResponse(cart, cartItemRepository.findByCart_CartId(cart.getCartId()));
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, CartItemRequest request) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart item not found"));
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(NOT_FOUND, "Cart item not found");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            cartItemRepository.delete(item);
        } else {
            validateQuantity(item.getProductVariant(), request.quantity());
            item.setQuantity(request.quantity());
            item.setNote(request.note());
            item.setTotalPrice(productMapper.finalPrice(item.getProductVariant()) * request.quantity());
            cartItemRepository.save(item);
        }
        return getCart(userId);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Cart item not found"));
        if (!item.getCart().getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(NOT_FOUND, "Cart item not found");
        }
        cartItemRepository.delete(item);
        return getCart(userId);
    }

    public int maxQuantity(Product product) {
        LocalDateTime now = LocalDateTime.now();
        boolean activeLimited = Boolean.TRUE.equals(product.getLimited())
                && product.getStartTime() != null
                && product.getEndTime() != null
                && !now.isBefore(product.getStartTime())
                && !now.isAfter(product.getEndTime());
        return activeLimited ? 3 : 5;
    }

    private void validateQuantity(ProductVariant variant, int quantity) {
        if (variant.getStock() != null && quantity > variant.getStock()) {
            throw new ResponseStatusException(BAD_REQUEST, "Quantity exceeds stock");
        }
        int max = maxQuantity(variant.getProduct());
        if (quantity > max) {
            throw new ResponseStatusException(BAD_REQUEST, "Maximum quantity is " + max);
        }
    }

    public CartResponse toResponse(Cart cart, List<CartItem> items) {
        List<CartItemResponse> responses = items.stream().map(this::toItemResponse).toList();
        int subtotal = responses.stream()
                .map(CartItemResponse::totalPrice)
                .filter(value -> value != null)
                .reduce(0, Integer::sum);
        return new CartResponse(cart.getCartId(), responses, subtotal);
    }

    public CartItemResponse toItemResponse(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();
        return new CartItemResponse(
                item.getId(),
                variant.getPvId(),
                product.getProductId(),
                product.getProductName(),
                variant.getImage(),
                variant.getSize() == null ? null : variant.getSize().getValue(),
                variant.getColor() == null ? null : variant.getColor().getColor(),
                item.getQuantity(),
                productMapper.finalPrice(variant),
                item.getTotalPrice(),
                item.getNote(),
                product.getLimited()
        );
    }

    private User user(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
    }

    private ProductVariant variant(Long pvId) {
        return productVariantRepository.findById(pvId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product variant not found"));
    }
}
