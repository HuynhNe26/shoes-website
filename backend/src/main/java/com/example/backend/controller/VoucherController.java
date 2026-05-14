package com.example.backend.controller;

import com.example.backend.dto.VoucherRequest;
import com.example.backend.dto.VoucherResponse;
import com.example.backend.security.RequestAuth;
import com.example.backend.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;
    private final RequestAuth requestAuth;

    @GetMapping("/api/vouchers/active")
    public List<VoucherResponse> active() {
        return voucherService.active();
    }

    @PostMapping("/api/admin/vouchers")
    public VoucherResponse create(@RequestBody VoucherRequest request, HttpServletRequest servletRequest) {
        requestAuth.requireAdmin(servletRequest);
        return voucherService.create(request);
    }

    @PutMapping("/api/admin/vouchers/{voucherId}")
    public VoucherResponse update(@PathVariable Long voucherId, @RequestBody VoucherRequest request, HttpServletRequest servletRequest) {
        requestAuth.requireAdmin(servletRequest);
        return voucherService.update(voucherId, request);
    }

    @DeleteMapping("/api/admin/vouchers/{voucherId}")
    public void delete(@PathVariable Long voucherId, HttpServletRequest servletRequest) {
        requestAuth.requireAdmin(servletRequest);
        voucherService.delete(voucherId);
    }
}
