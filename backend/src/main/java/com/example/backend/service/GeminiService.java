package com.example.backend.service;

import com.example.backend.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private static final ParameterizedTypeReference<Map<String, Object>> WEB_MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final AppProperties properties;
    private final WebClient.Builder webClientBuilder;

    public String generate(String prompt, String fallback) {
        if (!StringUtils.hasText(properties.getGemini().getApiKey())) {
            return fallback;
        }
        try {
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={key}",
                            properties.getGemini().getModel(),
                            properties.getGemini().getApiKey())
                    .bodyValue(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))))
                    .retrieve()
                    .bodyToMono(WEB_MAP_TYPE)
                    .block();
            return extractText(response, fallback);
        } catch (Exception exception) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<String, Object> response, String fallback) {
        if (response == null) {
            return fallback;
        }
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return fallback;
        }
        Map<String, Object> content = (Map<String, Object>) candidates.getFirst().get("content");
        if (content == null) {
            return fallback;
        }
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return fallback;
        }
        Object text = parts.getFirst().get("text");
        return text == null ? fallback : text.toString();
    }
}
