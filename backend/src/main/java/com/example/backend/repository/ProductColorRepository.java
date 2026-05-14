package com.example.backend.repository;

import com.example.backend.entity.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {
}
