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
@Table(name = "chat_ai")
public class ChatAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false, updatable = false)
    private Long chatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(length = 20, nullable = false)
    private String sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(length = 255)
    private String intent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id")
    private ProductVariant productVariant;

    private Integer quantity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
