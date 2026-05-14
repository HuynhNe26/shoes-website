package com.example.backend.service;

import com.example.backend.config.AppProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class SocialAuthService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final ParameterizedTypeReference<Map<String, Object>> WEB_MAP_TYPE = new ParameterizedTypeReference<>() {
    };

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final AppProperties properties;

    public SocialIdentity verifyGoogle(String idToken) {
        Map<String, Object> claims = webClientBuilder.build()
                .get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token={idToken}", idToken)
                .retrieve()
                .bodyToMono(WEB_MAP_TYPE)
                .block();

        if (claims == null || !"true".equals(String.valueOf(claims.get("email_verified")))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Google email is not verified");
        }
        validateAudience(claims.get("aud"), properties.getOauth().getGoogleClientId(), "Google");

        return new SocialIdentity(
                string(claims.get("email")),
                string(claims.get("given_name")),
                string(claims.get("family_name")),
                string(claims.get("picture"))
        );
    }

    public SocialIdentity verifyApple(String idToken) {
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid Apple token");
            }

            Map<String, Object> header = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[0]), MAP_TYPE);
            Map<String, Object> claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), MAP_TYPE);
            verifyAppleSignature(parts, header);

            if (!"https://appleid.apple.com".equals(string(claims.get("iss")))) {
                throw new ResponseStatusException(UNAUTHORIZED, "Invalid Apple issuer");
            }
            validateAudience(claims.get("aud"), properties.getOauth().getAppleClientId(), "Apple");
            long exp = claims.get("exp") instanceof Number number ? number.longValue() : Long.parseLong(string(claims.get("exp")));
            if (Instant.now().getEpochSecond() >= exp) {
                throw new ResponseStatusException(UNAUTHORIZED, "Expired Apple token");
            }

            String email = string(claims.get("email"));
            if (!StringUtils.hasText(email)) {
                throw new ResponseStatusException(UNAUTHORIZED, "Apple token must contain email on first login");
            }
            return new SocialIdentity(email, null, null, null);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(UNAUTHORIZED, "Cannot verify Apple token", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void verifyAppleSignature(String[] parts, Map<String, Object> header) throws Exception {
        Map<String, Object> keysResponse = webClientBuilder.build()
                .get()
                .uri(URI.create("https://appleid.apple.com/auth/keys"))
                .retrieve()
                .bodyToMono(WEB_MAP_TYPE)
                .block();
        List<Map<String, Object>> keys = (List<Map<String, Object>>) keysResponse.get("keys");
        Map<String, Object> key = keys.stream()
                .filter(item -> string(header.get("kid")).equals(string(item.get("kid"))))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Apple key not found"));

        BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(string(key.get("n"))));
        BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(string(key.get("e"))));
        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
        if (!signature.verify(Base64.getUrlDecoder().decode(parts[2]))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid Apple token signature");
        }
    }

    @SuppressWarnings("unchecked")
    private void validateAudience(Object actualAudience, String configuredAudience, String provider) {
        if (!StringUtils.hasText(configuredAudience)) {
            return;
        }
        boolean valid = actualAudience instanceof List<?> list
                ? list.contains(configuredAudience)
                : configuredAudience.equals(string(actualAudience));
        if (!valid) {
            throw new ResponseStatusException(UNAUTHORIZED, provider + " audience mismatch");
        }
    }

    private String string(Object value) {
        return value == null ? null : value.toString();
    }
}
