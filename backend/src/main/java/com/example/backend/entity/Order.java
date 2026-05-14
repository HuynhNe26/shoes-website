package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "order_code", length = 100)
    private String orderCode;

    @Column(name = "order_transaction", length = 150)
    private String orderTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(name = "tax_price")
    private Integer taxPrice;

    @Column(name = "transfer_price")
    private Integer transferPrice;

    @Column(name = "transfer_date")
    private LocalDate transferDate;

    @Column(name = "confirm_status")
    private Boolean confirmStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;

    @Column(length = 50)
    private String status;

    @Column(name = "refund_status")
    private Boolean refundStatus;

    @Column(name = "refund_time")
    private LocalDateTime refundTime;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> details = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (confirmStatus == null) {
            confirmStatus = false;
        }
        if (refundStatus == null) {
            refundStatus = false;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
