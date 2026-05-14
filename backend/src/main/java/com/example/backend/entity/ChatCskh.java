package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "chat_cskh")
public class ChatCskh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false, updatable = false)
    private Long chatId;

    @Column(name = "room_id", length = 255, nullable = false)
    private String roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_role", length = 20)
    private String senderRole;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    private Boolean read;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (read == null) {
            read = false;
        }
    }
}
