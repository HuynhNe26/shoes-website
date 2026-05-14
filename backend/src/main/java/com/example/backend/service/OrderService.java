package com.example.backend.service;

import com.example.backend.dto.CheckoutRequest;
import com.example.backend.dto.OrderItemResponse;
import com.example.backend.dto.OrderResponse;
import com.example.backend.dto.ShippingQuoteResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderVoucherRepository orderVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final ProductMapper productMapper;
    private final CartService cartService;
    private final ShippingService shippingService;
    private final EmailService emailService;

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Cart is empty"));
        List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
        if (items.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart is empty");
        }

        items.forEach(this::validateStockBeforeCheckout);

        int subTotal = items.stream().map(CartItem::getTotalPrice).reduce(0, Integer::sum);
        Voucher voucher = resolveVoucher(request.voucherCode(), subTotal);
        int voucherDiscount = discountFromVoucher(voucher, subTotal);
        String membership = user.getMembership() == null ? "MEMBER" : user.getMembership().getMemberName();
        int membershipDiscount = (subTotal - voucherDiscount) * shippingService.membershipDiscountPercent(membership) / 100;
        ShippingQuoteResponse shipping = shippingService.quote(request.destinationLatitude(), request.destinationLongitude(), user);
        int taxable = Math.max(0, subTotal - voucherDiscount - membershipDiscount);
        int tax = Math.round(taxable * 0.08f);
        int total = Math.max(0, taxable + tax + shipping.transferPrice());

        Order order = new Order();
        order.setOrderCode(nextOrderCode());
        order.setUser(user);
        order.setStatus("PENDING_PAYMENT");
        order.setTaxPrice(tax);
        order.setTransferPrice(shipping.transferPrice());
        order.setTransferDate(LocalDate.now());
        order.setTotalPrice(total);

        for (CartItem item : items) {
            ProductVariant variant = item.getProductVariant();
            variant.setStock((variant.getStock() == null ? 0 : variant.getStock()) - item.getQuantity());
            variant.setSoldQuantity((variant.getSoldQuantity() == null ? 0 : variant.getSoldQuantity()) + item.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProductVariant(variant);
            detail.setQuantity(item.getQuantity());
            detail.setPriceAtPurchase(productMapper.finalPrice(variant));
            detail.setTotalPrice(item.getTotalPrice());
            detail.setNote(item.getNote());
            order.getDetails().add(detail);
        }

        Order saved = orderRepository.save(order);
        if (voucher != null) {
            voucher.setUsedQuantity((voucher.getUsedQuantity() == null ? 0 : voucher.getUsedQuantity()) + 1);
            OrderVoucher orderVoucher = new OrderVoucher();
            orderVoucher.setOrder(saved);
            orderVoucher.setVoucher(voucher);
            orderVoucherRepository.save(orderVoucher);
        }
        cartItemRepository.deleteByCart(cart);
        emailService.sendInvoice(user, saved, saved.getDetails());

        return toResponse(saved, subTotal, voucherDiscount, membershipDiscount);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(order -> toResponse(order, null, null, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse detail(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        if (!order.getUser().getUserId().equals(userId)) {
            throw new ResponseStatusException(NOT_FOUND, "Order not found");
        }
        return toResponse(order, null, null, null);
    }

    public OrderResponse toResponse(Order order, Integer subTotal, Integer voucherDiscount, Integer membershipDiscount) {
        List<OrderItemResponse> items = order.getDetails().stream()
                .map(detail -> new OrderItemResponse(
                        detail.getProductVariant().getPvId(),
                        detail.getProductVariant().getProduct().getProductName(),
                        detail.getProductVariant().getSize() == null ? null : detail.getProductVariant().getSize().getValue(),
                        detail.getProductVariant().getColor() == null ? null : detail.getProductVariant().getColor().getColor(),
                        detail.getQuantity(),
                        detail.getPriceAtPurchase(),
                        detail.getTotalPrice()
                ))
                .toList();
        int inferredSubTotal = subTotal == null ? items.stream().map(OrderItemResponse::totalPrice).reduce(0, Integer::sum) : subTotal;
        return new OrderResponse(
                order.getOrderId(),
                order.getOrderCode(),
                order.getStatus(),
                order.getCreatedAt(),
                inferredSubTotal,
                voucherDiscount == null ? 0 : voucherDiscount,
                membershipDiscount == null ? 0 : membershipDiscount,
                order.getTaxPrice(),
                order.getTransferPrice(),
                order.getTotalPrice(),
                items
        );
    }

    private void validateStockBeforeCheckout(CartItem item) {
        ProductVariant variant = item.getProductVariant();
        int maxQuantity = cartService.maxQuantity(variant.getProduct());
        if (item.getQuantity() > maxQuantity) {
            throw new ResponseStatusException(BAD_REQUEST, "Maximum quantity is " + maxQuantity);
        }
        if (variant.getStock() == null || variant.getStock() < item.getQuantity()) {
            throw new ResponseStatusException(BAD_REQUEST, "Product is out of stock: " + variant.getProduct().getProductName());
        }
    }

    private Voucher resolveVoucher(String voucherCode, int subTotal) {
        if (!StringUtils.hasText(voucherCode)) {
            return null;
        }
        Voucher voucher = voucherRepository.findByVoucherCodeIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Voucher not found"));
        LocalDateTime now = LocalDateTime.now();
        boolean active = !Boolean.FALSE.equals(voucher.getStatus())
                && (voucher.getVoucherStart() == null || !now.isBefore(voucher.getVoucherStart()))
                && (voucher.getVoucherEnd() == null || !now.isAfter(voucher.getVoucherEnd()))
                && (voucher.getQuantity() == null || (voucher.getUsedQuantity() == null ? 0 : voucher.getUsedQuantity()) < voucher.getQuantity());
        if (!active || (voucher.getMinOrderValue() != null && subTotal < voucher.getMinOrderValue())) {
            throw new ResponseStatusException(BAD_REQUEST, "Voucher is not applicable");
        }
        return voucher;
    }

    private int discountFromVoucher(Voucher voucher, int subTotal) {
        if (voucher == null || voucher.getVoucherDiscount() == null) {
            return 0;
        }
        int discount = Boolean.TRUE.equals(voucher.getVoucherType())
                ? subTotal * voucher.getVoucherDiscount() / 100
                : voucher.getVoucherDiscount();
        if (voucher.getMaxReductionValue() != null && voucher.getMaxReductionValue() > 0) {
            discount = Math.min(discount, voucher.getMaxReductionValue());
        }
        return Math.min(discount, subTotal);
    }

    private String nextOrderCode() {
        return "SH" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
