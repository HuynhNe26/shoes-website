package com.example.backend.repository;

import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            select coalesce(sum(o.totalPrice), 0)
            from Order o
            where o.createdAt between :from and :to
            and lower(coalesce(o.status, '')) not in ('cancelled', 'refunded')
            """)
    Long sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
