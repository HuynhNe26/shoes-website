package com.example.backend.service;

import com.example.backend.dto.AiChatRequest;
import com.example.backend.dto.AiChatResponse;
import com.example.backend.dto.ProductCardResponse;
import com.example.backend.entity.ChatAi;
import com.example.backend.entity.ChatCskh;
import com.example.backend.entity.Embedding;
import com.example.backend.entity.ProductVariant;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatAiRepository;
import com.example.backend.repository.ChatCskhRepository;
import com.example.backend.repository.EmbeddingRepository;
import com.example.backend.repository.ProductVariantRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ChatAiRepository chatAiRepository;
    private final ChatCskhRepository chatCskhRepository;
    private final EmbeddingRepository embeddingRepository;
    private final CatalogService catalogService;
    private final ProductMapper productMapper;
    private final GeminiService geminiService;

    @Transactional
    public AiChatResponse chat(AuthPrincipal principal, AiChatRequest request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        String sessionId = StringUtils.hasText(request.sessionId()) ? request.sessionId() : UUID.randomUUID().toString();
        saveAiMessage(user, sessionId, "USER", request.message(), null, null, null, null);

        String intent = detectIntent(request.message());
        List<ProductCardResponse> suggestions = catalogService.recommend(null, request.message(), 4).stream()
                .limit(4)
                .toList();
        AiChatResponse response = switch (intent) {
            case "ADD_TO_CART" -> addToCartDraft(user, sessionId, request, suggestions);
            case "CSKH_HANDOFF" -> handoffToCskh(user, sessionId, request.message());
            default -> ragAnswer(user, sessionId, request.message(), suggestions);
        };
        saveAiMessage(user, sessionId, "AI", response.answer(), response.intent(), response.pvId(), response.quantity(), response.cskhRoomId());
        return response;
    }

    private AiChatResponse addToCartDraft(User user, String sessionId, AiChatRequest request, List<ProductCardResponse> suggestions) {
        if (request.pvId() == null || request.quantity() == null || request.quantity() <= 0) {
            return new AiChatResponse(
                    sessionId,
                    "Mình cần bạn chọn đúng size/màu và số lượng trước. Khi đủ thông tin, mình sẽ hiện nút lưu vào giỏ hàng.",
                    "ADD_TO_CART",
                    false,
                    request.pvId(),
                    request.quantity(),
                    null,
                    suggestions
            );
        }
        ProductVariant variant = productVariantRepository.findById(request.pvId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product variant not found"));
        String answer = "Đã đủ thông tin: %s, size %s, màu %s, số lượng %d. Bạn có thể bấm lưu vào giỏ hàng."
                .formatted(
                        variant.getProduct().getProductName(),
                        variant.getSize() == null ? "N/A" : variant.getSize().getValue(),
                        variant.getColor() == null ? "N/A" : variant.getColor().getColor(),
                        request.quantity()
                );
        return new AiChatResponse(sessionId, answer, "ADD_TO_CART", true, request.pvId(), request.quantity(), null, suggestions);
    }

    private AiChatResponse handoffToCskh(User user, String sessionId, String message) {
        String roomId = "user-" + user.getUserId() + "-" + sessionId;
        ChatCskh chat = new ChatCskh();
        chat.setRoomId(roomId);
        chat.setSenderId(user.getUserId());
        chat.setSenderRole("USER");
        chat.setMessage(message);
        chatCskhRepository.save(chat);
        return new AiChatResponse(sessionId, "Mình đã chuyển bạn sang CSKH. Nhân viên sẽ tiếp tục hỗ trợ trong phòng chat này.", "CSKH_HANDOFF", false, null, null, roomId, List.of());
    }

    private AiChatResponse ragAnswer(User user, String sessionId, String message, List<ProductCardResponse> suggestions) {
        String knowledge = embeddingRepository.searchKnowledge(message).stream()
                .map(Embedding::getContent)
                .filter(StringUtils::hasText)
                .reduce("", (left, right) -> left + "\n- " + right);
        String fallback = "Mình đã tìm một số lựa chọn phù hợp bên dưới. Bạn có thể hỏi thêm về size, màu hoặc yêu cầu mình chuẩn bị thêm vào giỏ.";
        String prompt = """
                Ban la AI tu van giay cho ecommerce. Tra loi tieng Viet ngan gon, huong dan khach mua dung san pham.
                Cau hoi: %s
                Kien thuc RAG: %s
                San pham goi y: %s
                """.formatted(message, knowledge, suggestions.stream().map(ProductCardResponse::productName).toList());
        String answer = geminiService.generate(prompt, fallback);
        return new AiChatResponse(sessionId, answer, "RAG_HELP", false, null, null, null, suggestions);
    }

    private String detectIntent(String message) {
        String normalized = message == null ? "" : message.toLowerCase();
        if (normalized.contains("cskh") || normalized.contains("nhân viên") || normalized.contains("tu van vien") || normalized.contains("chăm sóc")) {
            return "CSKH_HANDOFF";
        }
        if (normalized.contains("giỏ") || normalized.contains("mua") || normalized.contains("thêm")) {
            return "ADD_TO_CART";
        }
        return "RAG_HELP";
    }

    private void saveAiMessage(User user, String sessionId, String sender, String message, String intent, Long pvId, Integer quantity, String roomId) {
        ChatAi chat = new ChatAi();
        chat.setUser(user);
        chat.setSessionId(sessionId);
        chat.setSender(sender);
        chat.setMessage(message == null ? "" : message);
        chat.setIntent(intent);
        if (pvId != null) {
            productVariantRepository.findById(pvId).ifPresent(chat::setProductVariant);
        }
        chat.setQuantity(quantity);
        Map<String, Object> meta = new LinkedHashMap<>();
        if (roomId != null) {
            meta.put("cskhRoomId", roomId);
        }
        chat.setMeta(meta.isEmpty() ? null : meta);
        chatAiRepository.save(chat);
    }
}
