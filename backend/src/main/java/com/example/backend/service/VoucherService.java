package com.example.backend.service;

import com.example.backend.dto.VoucherRequest;
import com.example.backend.dto.VoucherResponse;
import com.example.backend.entity.Voucher;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<VoucherResponse> active() {
        return voucherRepository.findActive(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VoucherResponse create(VoucherRequest request) {
        Voucher voucher = new Voucher();
        apply(voucher, request);
        Voucher saved = voucherRepository.save(voucher);
        if (Boolean.TRUE.equals(request.notifyVipDiamond())) {
            userRepository.findByMembershipNames(List.of("VIP", "DIAMOND"))
                    .forEach(user -> emailService.sendVoucherNotice(user, saved));
        }
        return toResponse(saved);
    }

    @Transactional
    public VoucherResponse update(Long voucherId, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Voucher not found"));
        apply(voucher, request);
        return toResponse(voucherRepository.save(voucher));
    }

    @Transactional
    public void delete(Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Voucher not found"));
        voucherRepository.delete(voucher);
    }

    public VoucherResponse toResponse(Voucher voucher) {
        return new VoucherResponse(
                voucher.getVoucherId(),
                voucher.getVoucherCode(),
                voucher.getVoucherType(),
                voucher.getVoucherDiscount(),
                voucher.getMinOrderValue(),
                voucher.getMaxReductionValue(),
                voucher.getQuantity(),
                voucher.getUsedQuantity(),
                voucher.getDescription(),
                voucher.getContributor(),
                voucher.getContributorImage(),
                voucher.getVoucherStart(),
                voucher.getVoucherEnd(),
                voucher.getStatus()
        );
    }

    private void apply(Voucher voucher, VoucherRequest request) {
        voucher.setVoucherCode(request.voucherCode());
        voucher.setVoucherType(request.voucherType());
        voucher.setVoucherDiscount(request.voucherDiscount());
        voucher.setMinOrderValue(request.minOrderValue());
        voucher.setMaxReductionValue(request.maxReductionValue());
        voucher.setQuantity(request.quantity());
        voucher.setDescription(request.description());
        voucher.setContributor(request.contributor());
        voucher.setContributorImage(request.contributorImage());
        voucher.setVoucherStart(request.voucherStart());
        voucher.setVoucherEnd(request.voucherEnd());
        voucher.setStatus(request.status());
    }
}
