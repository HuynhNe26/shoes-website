package com.example.backend.repository;

import com.example.backend.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProduct_ProductId(Long productId);

    @Query("""
            select v from ProductVariant v
            where coalesce(v.stock, 0) <= :threshold
            order by v.stock asc
            """)
    List<ProductVariant> findLowStock(Integer threshold);
}
