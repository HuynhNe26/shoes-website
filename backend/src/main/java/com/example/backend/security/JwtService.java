package com.example.backend.security;

import com.example.backend.config.AppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class JwtService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AppProperties properties;
    private final ObjectMapper objectMapper;

    public AuthTokens createTokens(Long id, String email, String role) {
        return new AuthTokens(
                createToken(id, email, role, "access", properties.getJwt().getAccessTokenSeconds()),
                createToken(id, email, role, "refresh", properties.getJwt().getRefreshTokenSeconds())
        );
    }

    public String createToken(Long id, String email, String role, String tokenType, long ttlSeconds) {
        try {
            long now = Instant.now().getEpochSecond();
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", email);
            payload.put("id", id);
            payload.put("role", role);
            payload.put("token_type", tokenType);
            payload.put("iat", now);
            payload.put("exp", now + ttlSeconds);

            String headerPart = encode(objectMapper.writeValueAsBytes(header));
            String payloadPart = encode(objectMapper.writeValueAsBytes(payload));
            String unsigned = headerPart + "." + payloadPart;
            return unsigned + "." + sign(unsigned);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot create JWT", exception);
        }
    }

    public AuthPrincipal verify(String token, String expectedTokenType) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid token");
            }

            String unsigned = parts[0] + "." + parts[1];
            if (!MessageDigest.isEqual(sign(unsigned).getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), MAP_TYPE);
            String tokenType = stringValue(payload.get("token_type"));
            if (!expectedTokenType.equals(tokenType)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid token type");
            }

            long exp = longValue(payload.get("exp"));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new ResponseStatusException(UNAUTHORIZED, "Expired token");
            }

            return new AuthPrincipal(longValue(payload.get("id")), stringValue(payload.get("sub")), stringValue(payload.get("role")), tokenType);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid token", exception);
        }
    }

    private String sign(String unsignedToken) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return encode(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }
}
