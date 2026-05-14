package com.example.backend.security;

import com.example.backend.config.AppProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieService {
    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";

    private final AppProperties properties;

    public void writeAuthCookies(HttpServletResponse response, AuthTokens tokens) {
        addCookie(response, ACCESS_TOKEN, tokens.accessToken(), properties.getJwt().getAccessTokenSeconds());
        addCookie(response, REFRESH_TOKEN, tokens.refreshToken(), properties.getJwt().getRefreshTokenSeconds());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN, "", 0);
        addCookie(response, REFRESH_TOKEN, "", 0);
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite(properties.isCookieSecure() ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
