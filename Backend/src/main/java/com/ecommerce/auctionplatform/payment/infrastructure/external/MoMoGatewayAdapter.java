package com.ecommerce.auctionplatform.payment.infrastructure.external;

import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import com.ecommerce.auctionplatform.shared.infrastructure.utils.PaymentUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoMoGatewayAdapter implements PaymentGatewayPort {
    @Value("${momo.partner-code:}")
    String partnerCode;
    @Value("${momo.access-key:}")
    String accessKey;
    @Value("${momo.secret-key:}")
    String secretKey;
    @Value("${momo.api-url:}")
    String apiUrl;
    @Value("${momo.return-url:}")
    String configuredReturnUrl;
    @Value("${momo.notify-url:}")
    String configuredNotifyUrl;
    @Value("${momo.request-type:captureWallet}")
    String requestType;

    @Override
    public GatewayPaymentResult createPayment(GatewayPaymentRequest request) {
        String orderId = request.transactionId().toString();
        String amount = request.amount().toBigIntegerExact().toString();
        String orderInfo = textOrDefault(request.orderInfo(), "Nap tien vao vi");
        String redirectUrl = textOrDefault(request.returnUrl(), configuredReturnUrl);
        String ipnUrl = textOrDefault(request.notifyUrl(), configuredNotifyUrl);
        String requestId = UUID.randomUUID().toString();
        String extraData = "";

        String rawHash = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        Map<String, Object> body = new HashMap<>();
        body.put("partnerCode", partnerCode);
        body.put("requestId", requestId);
        body.put("amount", amount);
        body.put("orderId", orderId);
        body.put("orderInfo", orderInfo);
        body.put("redirectUrl", redirectUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("lang", "vi");
        body.put("extraData", extraData);
        body.put("requestType", requestType);
        body.put("autoCapture", true);
        body.put("signature", PaymentUtils.calculateHmacSHA256(rawHash, secretKey));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<Map> response = new RestTemplate().postForEntity(
                    apiUrl, new HttpEntity<>(body, headers), Map.class);
            Map<?, ?> responseBody = response.getBody();
            if (responseBody == null || !"0".equals(String.valueOf(responseBody.get("resultCode")))) {
                String message = responseBody == null ? "Unknown error" : String.valueOf(responseBody.get("message"));
                throw new IllegalStateException("Tạo thanh toán MoMo thất bại: " + message);
            }
            return new GatewayPaymentResult(
                    String.valueOf(responseBody.get("payUrl")),
                    orderId,
                    "Tạo link thanh toán MoMo thành công");
        } catch (RuntimeException exception) {
            log.error("Error calling MoMo API", exception);
            throw new IllegalStateException("Không thể kết nối cổng thanh toán MoMo", exception);
        }
    }

    @Override
    public GatewayCallbackResult verifyCallback(Map<String, String> data) {
        String rawHash = "accessKey=" + accessKey
                + "&amount=" + data.get("amount")
                + "&extraData=" + data.get("extraData")
                + "&message=" + data.get("message")
                + "&orderId=" + data.get("orderId")
                + "&orderInfo=" + data.get("orderInfo")
                + "&orderType=" + data.get("orderType")
                + "&partnerCode=" + partnerCode
                + "&payType=" + data.get("payType")
                + "&requestId=" + data.get("requestId")
                + "&responseTime=" + data.get("responseTime")
                + "&resultCode=" + data.get("resultCode")
                + "&transId=" + data.get("transId");
        boolean valid = PaymentUtils.calculateHmacSHA256(rawHash, secretKey)
                .equals(data.get("signature"));
        if (!valid) {
            log.warn("Rejected MoMo callback with an invalid signature for order {}", data.get("orderId"));
        }
        return new GatewayCallbackResult(
                parseUuid(data.get("orderId")),
                data.get("transId"),
                parseAmount(data.get("amount")),
                valid,
                valid && "0".equals(data.get("resultCode")),
                valid ? data.get("message") : "Chữ ký không hợp lệ",
                data.get("responseTime"),
                data.toString());
    }

    @Override
    public PaymentMethod gatewayType() {
        return PaymentMethod.MOMO;
    }

    private UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
