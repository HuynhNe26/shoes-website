package com.example.backend.repository;

import com.example.backend.entity.Embedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmbeddingRepository extends JpaRepository<Embedding, Long> {
    @Query(value = """
            select * from embedding
            where (:keyword is null or lower(content) like lower(concat('%', :keyword, '%')))
            order by created_at desc
            limit 6
            """, nativeQuery = true)
    List<Embedding> searchKnowledge(@Param("keyword") String keyword);
}
