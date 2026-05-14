package com.example.backend.repository;

import com.example.backend.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select distinct p from Product p
            where (:keyword is null
                or lower(p.productName) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(p.description, '')) like lower(concat('%', :keyword, '%')))
            order by p.createdAt desc
            """)
    List<Product> searchProducts(@Param("keyword") String keyword);

    @Query("""
            select distinct p from Product p
            join p.variants v
            where coalesce(v.status, true) = true
            and v.priceDiscount is not null
            and v.priceDiscount > 0
            and (v.price is null or v.priceDiscount < v.price)
            order by p.createdAt desc
            """)
    List<Product> findSaleProducts();

    @Query("""
            select distinct p from Product p
            join p.variants v
            where coalesce(v.status, true) = true
            order by coalesce(v.soldQuantity, 0) desc
            """)
    List<Product> findBestSellers(Pageable pageable);

    @Query("""
            select p from Product p
            where p.limited = true
            and (:now between p.startTime and p.endTime)
            order by p.endTime asc
            """)
    List<Product> findActiveLimited(@Param("now") LocalDateTime now);
}
