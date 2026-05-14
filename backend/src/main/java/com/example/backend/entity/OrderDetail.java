package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id")
    private ProductVariant productVariant;

    private Integer quantity;

    @Column(name = "price_at_purchase")
    private Integer priceAtPurchase;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(length = 200)
    private String note;
}
