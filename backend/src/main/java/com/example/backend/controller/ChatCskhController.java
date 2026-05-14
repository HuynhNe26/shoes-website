package com.example.backend.controller;

import com.example.backend.dto.ChatCskhRequest;
import com.example.backend.dto.ChatCskhResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.ChatCskhService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/chat/cskh")
@RequiredArgsConstructor
public class ChatCskhController {
    private final ChatCskhService chatCskhService;
    private final RequestAuth requestAuth;

    @GetMapping("/{roomId}")
    public List<ChatCskhResponse> messages(@PathVariable String roomId, HttpServletRequest request) {
        requestAuth.optional(request).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required"));
        return chatCskhService.messages(roomId);
    }

    @PostMapping("/{roomId}/messages")
    public ChatCskhResponse send(@PathVariable String roomId, @RequestBody ChatCskhRequest chatRequest, HttpServletRequest request) {
        return chatCskhService.send(
                roomId,
                requestAuth.optional(request).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required")),
                chatRequest.message()
        );
    }

    @MessageMapping("/chat/{roomId}")
    public void socketSend(@DestinationVariable String roomId, ChatCskhRequest chatRequest) {
        // REST endpoint is preferred because it carries cookie auth; this keeps STOMP clients able to publish demo messages.
    }
}
