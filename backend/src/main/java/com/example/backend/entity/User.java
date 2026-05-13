package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(length = 255, nullable = false)
    private String password;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(length = 10, nullable = false)
    private String gender;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "phone_number", length = 13, nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer point;

    @Column(columnDefinition = "jsonb")
    private String favorite;

    @Column(name = "search_history", columnDefinition = "jsonb")
    private String searchHistory;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Membership membership;

    @Column(name = "member_start_time", nullable = false)
    private LocalDateTime memberStartTime;

    @Column(name = "member_end_time", nullable = false)
    private LocalDateTime memberEndTime;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "logout_time", nullable = false)
    private LocalDateTime logoutTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 50, nullable = false)
    private String status;

}
