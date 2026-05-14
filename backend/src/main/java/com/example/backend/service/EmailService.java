package com.example.backend.service;

import com.example.backend.config.AppProperties;
import com.example.backend.entity.Order;
import com.example.backend.entity.OrderDetail;
import com.example.backend.entity.User;
import com.example.backend.entity.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final AppProperties properties;
    private final WebClient.Builder webClientBuilder;

    public void sendInvoice(User user, Order order, List<OrderDetail> details) {
        if (!StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(properties.getResend().getApiKey())) {
            return;
        }
        String rows = details.stream()
                .map(detail -> "<tr><td>%s</td><td>%d</td><td>%s</td></tr>".formatted(
                        detail.getProductVariant().getProduct().getProductName(),
                        detail.getQuantity(),
                        money(detail.getTotalPrice())
                ))
                .reduce("", String::concat);
        String html = """
                <h2>Hoa don dien tu %s</h2>
                <p>Cam on ban da mua hang tai Shoes Website.</p>
                <table border="1" cellpadding="8" cellspacing="0">
                  <thead><tr><th>San pham</th><th>So luong</th><th>Tong</th></tr></thead>
                  <tbody>%s</tbody>
                </table>
                <p><strong>Tong thanh toan: %s</strong></p>
                """.formatted(order.getOrderCode(), rows, money(order.getTotalPrice()));
        send(user.getEmail(), "Hoa don dien tu " + order.getOrderCode(), html);
    }

    public void sendVoucherNotice(User user, Voucher voucher) {
        if (!StringUtils.hasText(user.getEmail()) || !StringUtils.hasText(properties.getResend().getApiKey())) {
            return;
        }
        String html = """
                <h2>Voucher moi danh rieng cho thanh vien than thiet</h2>
                <p>Ma <strong>%s</strong> dang san sang cho ban.</p>
                <p>%s</p>
                """.formatted(voucher.getVoucherCode(), voucher.getDescription() == null ? "" : voucher.getDescription());
        send(user.getEmail(), "Voucher moi: " + voucher.getVoucherCode(), html);
    }

    private void send(String to, String subject, String html) {
        webClientBuilder.build()
                .post()
                .uri("https://api.resend.com/emails")
                .header("Authorization", "Bearer " + properties.getResend().getApiKey())
                .bodyValue(Map.of(
                        "from", properties.getResend().getFromEmail(),
                        "to", List.of(to),
                        "subject", subject,
                        "html", html
                ))
                .retrieve()
                .toBodilessEntity()
                .onErrorComplete()
                .block();
    }

    private String money(Integer amount) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(amount == null ? 0 : amount);
    }
}
