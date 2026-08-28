package com.ecommerce.auctionplatform.payment.infrastructure.external;

import com.ecommerce.auctionplatform.payment.application.port.out.PaymentGatewayPort;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import com.ecommerce.auctionplatform.shared.infrastructure.utils.PaymentUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayGatewayAdapter implements PaymentGatewayPort {
    @Value("${vnpay.tmn-code:}")
    String tmnCode;
    @Value("${vnpay.hash-secret:}")
    String hashSecret;
    @Value("${vnpay.api-url:}")
    String apiUrl;
    @Value("${vnpay.return-url:}")
    String configuredReturnUrl;
    @Value("${vnpay.version:2.1.0}")
    String version;
    @Value("${vnpay.command:pay}")
    String command;
    @Value("${vnpay.order-type:other}")
    String orderType;

    @Override
    public GatewayPaymentResult createPayment(GatewayPaymentRequest request) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", version);
        params.put("vnp_Command", command);
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", request.amount().multiply(BigDecimal.valueOf(100)).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_CreateDate", PaymentUtils.getVNPayTimestamp());
        params.put("vnp_ExpireDate", PaymentUtils.getVNPayExpireTime(15));
        params.put("vnp_TxnRef", request.transactionId().toString());
        params.put("vnp_OrderInfo", textOrDefault(request.orderInfo(), "Nap tien vao vi"));
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", textOrDefault(request.returnUrl(), configuredReturnUrl));
        params.put("vnp_IpAddr", "127.0.0.1");

        String hashData = encodedQuery(params);
        params.put("vnp_SecureHash", PaymentUtils.calculateHmacSHA512(hashData, hashSecret));
        return new GatewayPaymentResult(
                apiUrl + "?" + encodedQuery(params),
                request.transactionId().toString(),
                "Created payment URL successfully");
    }

    @Override
    public GatewayCallbackResult verifyCallback(Map<String, String> callbackData) {
        String suppliedHash = callbackData.get("vnp_SecureHash");
        Map<String, String> signedParams = new TreeMap<>();
        callbackData.forEach((key, value) -> {
            if (value != null && !key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")) {
                signedParams.put(key, value);
            }
        });
        boolean valid = suppliedHash != null
                && PaymentUtils.calculateHmacSHA512(encodedQuery(signedParams), hashSecret).equals(suppliedHash);
        if (!valid) {
            log.warn("Rejected VNPay callback with an invalid signature for transaction {}",
                    callbackData.get("vnp_TxnRef"));
        }
        return new GatewayCallbackResult(
                parseUuid(callbackData.get("vnp_TxnRef")),
                callbackData.get("vnp_TransactionNo"),
                parseAmount(callbackData.get("vnp_Amount")),
                valid,
                valid && "00".equals(callbackData.get("vnp_ResponseCode")),
                valid
                        ? ("00".equals(callbackData.get("vnp_ResponseCode"))
                            ? "Thanh toán thành công"
                            : "Thanh toán thất bại - Mã lỗi: " + callbackData.get("vnp_ResponseCode"))
                        : "Chữ ký không hợp lệ",
                callbackData.get("vnp_PayDate"),
                callbackData.toString());
    }

    @Override
    public PaymentMethod gatewayType() {
        return PaymentMethod.VNPAY;
    }

    private String encodedQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
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
            return new BigDecimal(value).movePointLeft(2);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
