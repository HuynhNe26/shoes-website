package com.example.backend.repository;

import com.example.backend.entity.ShoeSize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoeSizeRepository extends JpaRepository<ShoeSize, Long> {
}
