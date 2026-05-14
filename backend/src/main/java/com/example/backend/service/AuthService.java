package com.example.backend.service;

import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.entity.Admin;
import com.example.backend.entity.AdminLoginLog;
import com.example.backend.entity.Membership;
import com.example.backend.entity.User;
import com.example.backend.repository.AdminLoginLogRepository;
import com.example.backend.repository.AdminRepository;
import com.example.backend.repository.MembershipRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.AuthPrincipal;
import com.example.backend.security.AuthTokens;
import com.example.backend.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final MembershipRepository membershipRepository;
    private final AdminLoginLogRepository adminLoginLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SocialAuthService socialAuthService;

    @Transactional
    public LoginResult register(RegisterRequest request) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email and password are required");
        }
        userRepository.findByEmailIgnoreCase(request.email()).ifPresent(user -> {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        });

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setGender(request.gender());
        user.setAddress(request.address());
        user.setDateOfBirth(request.dateOfBirth());
        user.setPhoneNumber(request.phoneNumber());
        user.setFavorite(request.favorite());
        user.setPoint(1);
        user.setStatus("ACTIVE");
        user.setMembership(defaultMembership());
        user.setMemberStartTime(LocalDateTime.now());
        userRepository.save(user);

        return userLoginResult(user);
    }

    @Transactional
    public LoginResult login(LoginRequest request, HttpServletRequest servletRequest) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new ResponseStatusException(BAD_REQUEST, "Email and password are required");
        }

        return userRepository.findByEmailIgnoreCase(request.email())
                .map(user -> loginUser(user, request.password()))
                .or(() -> adminRepository.findByEmailIgnoreCase(request.email())
                        .map(admin -> loginAdmin(admin, request.password(), servletRequest)))
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));
    }

    @Transactional
    public LoginResult google(String idToken) {
        return socialLogin(socialAuthService.verifyGoogle(idToken));
    }

    @Transactional
    public LoginResult apple(String idToken) {
        return socialLogin(socialAuthService.verifyApple(idToken));
    }

    @Transactional(readOnly = true)
    public AuthResponse me(AuthPrincipal principal) {
        if (principal.isAdmin()) {
            Admin admin = adminRepository.findById(principal.id())
                    .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Admin not found"));
            return new AuthResponse(admin.getAdminId(), admin.getEmail(), admin.getFirstName(), admin.getLastName(), "ADMIN", null, null);
        }
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "User not found"));
        return toUserResponse(user);
    }

    public LoginResult refresh(AuthPrincipal refreshPrincipal) {
        AuthTokens tokens = jwtService.createTokens(refreshPrincipal.id(), refreshPrincipal.email(), refreshPrincipal.role());
        return new LoginResult(me(refreshPrincipal), tokens);
    }

    private LoginResult loginUser(User user, String password) {
        if (!StringUtils.hasText(user.getPassword()) || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        }
        user.setLoginTime(LocalDateTime.now());
        return userLoginResult(user);
    }

    private LoginResult loginAdmin(Admin admin, String password, HttpServletRequest request) {
        if (!StringUtils.hasText(admin.getPassword()) || !passwordEncoder.matches(password, admin.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
        }

        AdminLoginLog log = new AdminLoginLog();
        log.setAdmin(admin);
        log.setIpAddress(request.getRemoteAddr());
        log.setUserAgent(request.getHeader("User-Agent"));
        log.setLoginTime(LocalDateTime.now());
        log.setStatus("SUCCESS");
        adminLoginLogRepository.save(log);

        AuthResponse response = new AuthResponse(admin.getAdminId(), admin.getEmail(), admin.getFirstName(), admin.getLastName(), "ADMIN", null, null);
        return new LoginResult(response, jwtService.createTokens(admin.getAdminId(), admin.getEmail(), "ADMIN"));
    }

    private LoginResult socialLogin(SocialIdentity identity) {
        if (!StringUtils.hasText(identity.email())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Social email is required");
        }

        User user = userRepository.findByEmailIgnoreCase(identity.email()).orElseGet(() -> {
            User created = new User();
            created.setEmail(identity.email().trim().toLowerCase());
            created.setFirstName(identity.firstName());
            created.setLastName(identity.lastName());
            created.setImage(identity.image());
            created.setPoint(1);
            created.setStatus("ACTIVE");
            created.setMembership(defaultMembership());
            created.setMemberStartTime(LocalDateTime.now());
            return created;
        });
        user.setLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return userLoginResult(user);
    }

    private LoginResult userLoginResult(User user) {
        AuthTokens tokens = jwtService.createTokens(user.getUserId(), user.getEmail(), "USER");
        return new LoginResult(toUserResponse(user), tokens);
    }

    private Membership defaultMembership() {
        return membershipRepository.findFirstByMemberNameIgnoreCase("MEMBER")
                .or(() -> membershipRepository.findTopByPointLessThanEqualOrderByPointDesc(1))
                .orElse(null);
    }

    private AuthResponse toUserResponse(User user) {
        String membershipName = user.getMembership() == null ? null : user.getMembership().getMemberName();
        return new AuthResponse(user.getUserId(), user.getEmail(), user.getFirstName(), user.getLastName(), "USER", membershipName, user.getPoint());
    }

    public record LoginResult(AuthResponse response, AuthTokens tokens) {
    }
}
