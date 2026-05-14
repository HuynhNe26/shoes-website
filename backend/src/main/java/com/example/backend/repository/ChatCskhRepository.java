package com.example.backend.repository;

import com.example.backend.entity.ChatCskh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatCskhRepository extends JpaRepository<ChatCskh, Long> {
    List<ChatCskh> findTop80ByRoomIdOrderByCreatedAtAsc(String roomId);
}
