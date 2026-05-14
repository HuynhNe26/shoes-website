package com.example.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
@RequiredArgsConstructor
public class RequestAuth {
    private final JwtService jwtService;

    public AuthPrincipal requireUser(HttpServletRequest request) {
        AuthPrincipal principal = readAccessPrincipal(request);
        if (!principal.isUser()) {
            throw new ResponseStatusException(FORBIDDEN, "User permission required");
        }
        return principal;
    }

    public AuthPrincipal requireAdmin(HttpServletRequest request) {
        AuthPrincipal principal = readAccessPrincipal(request);
        if (!principal.isAdmin()) {
            throw new ResponseStatusException(FORBIDDEN, "Admin permission required");
        }
        return principal;
    }

    public Optional<AuthPrincipal> optional(HttpServletRequest request) {
        try {
            return Optional.of(readAccessPrincipal(request));
        } catch (ResponseStatusException exception) {
            return Optional.empty();
        }
    }

    private AuthPrincipal readAccessPrincipal(HttpServletRequest request) {
        String token = bearerToken(request).or(() -> cookieToken(request, CookieService.ACCESS_TOKEN))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required"));
        return jwtService.verify(token, "access");
    }

    private Optional<String> bearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return Optional.of(authorization.substring(7));
        }
        return Optional.empty();
    }

    public Optional<String> cookieToken(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
