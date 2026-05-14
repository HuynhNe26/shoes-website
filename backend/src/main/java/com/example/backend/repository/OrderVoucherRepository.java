package com.example.backend.repository;

import com.example.backend.entity.OrderVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, Long> {
}
