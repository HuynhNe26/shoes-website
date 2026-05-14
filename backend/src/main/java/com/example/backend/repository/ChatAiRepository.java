package com.example.backend.repository;

import com.example.backend.entity.ChatAi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatAiRepository extends JpaRepository<ChatAi, Long> {
    List<ChatAi> findTop30BySessionIdOrderByCreatedAtDesc(String sessionId);
}
