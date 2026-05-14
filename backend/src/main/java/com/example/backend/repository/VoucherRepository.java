package com.example.backend.repository;

import com.example.backend.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByVoucherCodeIgnoreCase(String voucherCode);

    @Query("""
            select v from Voucher v
            where coalesce(v.status, true) = true
            and (v.voucherStart is null or v.voucherStart <= :now)
            and (v.voucherEnd is null or v.voucherEnd >= :now)
            and (v.quantity is null or coalesce(v.usedQuantity, 0) < v.quantity)
            order by v.voucherEnd asc nulls last
            """)
    List<Voucher> findActive(@Param("now") LocalDateTime now);
}
