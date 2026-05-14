package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "voucher_id", nullable = false, updatable = false)
    private Long voucherId;

    @Column(name = "voucher_code", length = 100)
    private String voucherCode;

    @Column(name = "voucher_type")
    private Boolean voucherType;

    @Column(name = "voucher_discount")
    private Integer voucherDiscount;

    @Column(name = "min_order_value")
    private Integer minOrderValue;

    @Column(name = "max_reduction_value")
    private Integer maxReductionValue;

    private Integer quantity;

    @Column(name = "used_quantity")
    private Integer usedQuantity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String contributor;

    @Column(name = "contributor_image", columnDefinition = "TEXT")
    private String contributorImage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "voucher_start")
    private LocalDateTime voucherStart;

    @Column(name = "voucher_end")
    private LocalDateTime voucherEnd;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private Boolean status;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (usedQuantity == null) {
            usedQuantity = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
