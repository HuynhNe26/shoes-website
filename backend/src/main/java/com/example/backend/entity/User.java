package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(length = 10)
    private String gender;

    @Column(length = 255)
    private String address;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "phone_number", length = 13)
    private String phoneNumber;

    private Integer point = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> favorite;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_history", columnDefinition = "jsonb")
    private Map<String, Object> searchHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Membership membership;

    @Column(name = "member_start_time")
    private LocalDateTime memberStartTime;

    @Column(name = "member_end_time")
    private LocalDateTime memberEndTime;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 50)
    private String status;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (point == null) {
            point = 1;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
