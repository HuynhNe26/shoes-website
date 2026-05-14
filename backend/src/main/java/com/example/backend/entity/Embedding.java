package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "embedding")
public class Embedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "embedding_id", nullable = false, updatable = false)
    private Long embeddingId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "vector(1536)")
    private String embedding;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
