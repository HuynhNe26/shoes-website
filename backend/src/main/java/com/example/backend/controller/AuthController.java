package com.example.backend.controller;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.SocialLoginRequest;
import com.example.backend.security.CookieService;
import com.example.backend.security.JwtService;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;
    private final RequestAuth requestAuth;
    private final JwtService jwtService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.register(request);
        cookieService.writeAuthCookies(response, result.tokens());
        return result.response();
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse response) {
        AuthService.LoginResult result = authService.login(request, servletRequest);
        cookieService.writeAuthCookies(response, result.tokens());
        return result.response();
    }

    @PostMapping("/google")
    public AuthResponse google(@RequestBody SocialLoginRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.google(request.idToken());
        cookieService.writeAuthCookies(response, result.tokens());
        return result.response();
    }

    @PostMapping("/apple")
    public AuthResponse apple(@RequestBody SocialLoginRequest request, HttpServletResponse response) {
        AuthService.LoginResult result = authService.apple(request.idToken());
        cookieService.writeAuthCookies(response, result.tokens());
        return result.response();
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = requestAuth.cookieToken(request, CookieService.REFRESH_TOKEN)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Refresh token required"));
        AuthService.LoginResult result = authService.refresh(jwtService.verify(refreshToken, "refresh"));
        cookieService.writeAuthCookies(response, result.tokens());
        return result.response();
    }

    @GetMapping("/me")
    public AuthResponse me(HttpServletRequest request) {
        return authService.me(requestAuth.optional(request).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authentication required")));
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        cookieService.clearAuthCookies(response);
    }
}
