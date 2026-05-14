package com.example.backend.service;

import com.example.backend.dto.ChatCskhResponse;
import com.example.backend.entity.ChatCskh;
import com.example.backend.repository.ChatCskhRepository;
import com.example.backend.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatCskhService {
    private final ChatCskhRepository chatCskhRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<ChatCskhResponse> messages(String roomId) {
        return chatCskhRepository.findTop80ByRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatCskhResponse send(String roomId, AuthPrincipal principal, String message) {
        ChatCskh chat = new ChatCskh();
        chat.setRoomId(roomId);
        chat.setSenderId(principal.id());
        chat.setSenderRole(principal.role());
        chat.setMessage(message);
        ChatCskhResponse response = toResponse(chatCskhRepository.save(chat));
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
        return response;
    }

    private ChatCskhResponse toResponse(ChatCskh chat) {
        return new ChatCskhResponse(
                chat.getChatId(),
                chat.getRoomId(),
                chat.getSenderId(),
                chat.getSenderRole(),
                chat.getMessage(),
                chat.getRead(),
                chat.getCreatedAt()
        );
    }
}
