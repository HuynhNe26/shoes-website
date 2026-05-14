package com.example.backend.repository;

import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_CartId(Long cartId);

    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant productVariant);

    void deleteByCart(Cart cart);
}
