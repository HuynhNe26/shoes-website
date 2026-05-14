package com.example.backend.controller;

import com.example.backend.dto.AiChatRequest;
import com.example.backend.dto.AiChatResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.AiChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;
    private final RequestAuth requestAuth;

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestBody AiChatRequest chatRequest, HttpServletRequest request) {
        return aiChatService.chat(requestAuth.requireUser(request), chatRequest);
    }
}
