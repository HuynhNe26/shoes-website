package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "color")
public class ProductColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id", nullable = false, updatable = false)
    private Long colorId;

    @Column(name = "color", length = 100)
    private String color;

    private Boolean status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
