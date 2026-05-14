package com.example.backend.security;

public record AuthPrincipal(Long id, String email, String role, String tokenType) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "USER".equalsIgnoreCase(role);
    }
}
