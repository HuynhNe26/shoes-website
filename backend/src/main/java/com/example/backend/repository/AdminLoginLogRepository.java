package com.example.backend.repository;

import com.example.backend.entity.AdminLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminLoginLogRepository extends JpaRepository<AdminLoginLog, Long> {
}
