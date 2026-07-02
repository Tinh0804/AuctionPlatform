package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.entity.Transaction;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.entity.enums.PaymentMethod;
import com.ecommerce.auctionplatform.entity.enums.TransactionStatus;
import com.ecommerce.auctionplatform.entity.enums.TransactionType;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.TransactionRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
import com.ecommerce.auctionplatform.utils.PaymentUtils;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VNPayService {

    final WalletRepository walletRepository;
    final UserRepository userRepository;
    final TransactionRepository transactionRepository;

    @Value("${vnpay.tmn-code:}")
    String tmnCode;

    @Value("${vnpay.hash-secret:}")
    String hashSecret;

    @Value("${vnpay.api-url:}")
    String apiUrl;

    @Value("${vnpay.return-url:}")
    String returnUrl;

    @Value("${vnpay.version:2.1.0}")
    String version;

    @Value("${vnpay.command:pay}")
    String command;

    @Value("${vnpay.order-type:other}")
    String orderType;

    public PaymentResponse createPayment(PaymentRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .status(TransactionStatus.PENDING)
                .gatewayProvider(PaymentMethod.VNPAY.name())
                .referenceId(UUID.randomUUID())
                .note(request.getOrderInfo())
                .build();
        
        transaction = transactionRepository.save(transaction);

        String vnp_TxnRef = transaction.getId().toString();
        String vnpCreateDate = PaymentUtils.getVNPayTimestamp();
        String vnp_ExpireDate = PaymentUtils.getVNPayExpireTime(15);
        
        // vnp_Amount in VNPay is multiplied by 100
        long amount = Math.round(request.getAmount().longValue() * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", version);
        vnp_Params.put("vnp_Command", command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_CreateDate",vnpCreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", request.getOrderInfo() != null ? request.getOrderInfo() : "Nap tien vao vi");
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1"); // In production, get real IP


        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            if (hashData.length() > 0) {
                hashData.append("&");
            }
            hashData.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());
        }

        String vnp_SecureHash = PaymentUtils.calculateHmacSHA512(hashData.toString(), hashSecret);
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);
        String paymentUrl = buildUrlWithEncode(apiUrl, vnp_Params);

        return PaymentResponse.builder()
                .status("SUCCESS")
                .message("Created payment URL successfully")
                .paymentUrl(paymentUrl)
                .orderId(vnp_TxnRef)
                .transactionId(transaction.getId().toString())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.VNPAY.name())
                .build();
    }

    @Transactional
    public PaymentCallbackResponse processCallback(Map<String, String> params) {
        log.info("Received VNPay Callback: {}", params);
        
        String vnp_SecureHash = params.get("vnp_SecureHash");
        if (vnp_SecureHash == null) {
            return PaymentCallbackResponse.builder()
                    .paymentStatus("FAILED")
                    .message("Thiếu chữ ký VNPay")
                    .paymentMethod("VNPAY")
                    .build();
        }

        Map<String, String> sortedParams = new TreeMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null
                    && !entry.getKey().equals("vnp_SecureHash")
                    && !entry.getKey().equals("vnp_SecureHashType")) {

                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {

            if (entry.getValue() != null && !entry.getValue().isEmpty()) {

                if (hashData.length() > 0) {
                    hashData.append("&");
                }

                hashData.append(entry.getKey())
                        .append("=")
                        .append(java.net.URLEncoder.encode(
                                entry.getValue(),
                                java.nio.charset.StandardCharsets.US_ASCII
                        ));
            }
        }

        String expectedSignature = PaymentUtils.calculateHmacSHA512(hashData.toString(), hashSecret);
        
        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");
        long actualAmount = amountStr != null ? Long.parseLong(amountStr) / 100 : 0L;
        String payDate = params.get("vnp_PayDate");

        if (!expectedSignature.equals(vnp_SecureHash)) {
            log.error("VNPay Callback Signature Validation Failed! Expected: {}, Actual: {}", expectedSignature, vnp_SecureHash);
            return PaymentCallbackResponse.builder()
                    .orderId(orderId)
                    .transactionId(transactionNo)
                    .amount(actualAmount)
                    .paymentStatus("FAILED")
                    .message("Chữ ký không hợp lệ")
                    .paymentMethod("VNPAY")
                    .build();
        }

        Transaction transaction = transactionRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction {} is already processed (Status: {})", orderId, transaction.getStatus());
            return PaymentCallbackResponse.builder()
                    .orderId(orderId)
                    .transactionId(transactionNo)
                    .amount(actualAmount)
                    .paymentStatus(transaction.getStatus().name())
                    .paymentMethod(PaymentMethod.VNPAY.name())
                    .message("Transaction already processed")
                    .paymentTime(payDate)
                    .build();
        }

        transaction.setGatewayResponse(params.toString());
        transaction.setGatewayTxId(transactionNo);

        String paymentStatus = "00".equals(responseCode) ? "SUCCESS" : "FAILED";

        String message = "00".equals(responseCode)
                ? "Thanh toán thành công"
                : "Thanh toán thất bại - Mã lỗi: " + responseCode;

        if ("SUCCESS".equals(paymentStatus)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            
            Wallet wallet = transaction.getWallet();
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(transaction.getAmount()));
            walletRepository.save(wallet);
            log.info("Successfully added {} to wallet {}", transaction.getAmount(), wallet.getId());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.info("VNPay payment failed for transaction {}. Response code: {}", orderId, responseCode);
        }

        transactionRepository.save(transaction);

        return PaymentCallbackResponse.builder()
                .orderId(orderId)
                .transactionId(transactionNo)
                .amount(actualAmount)
                .paymentStatus(paymentStatus)
                .paymentMethod(PaymentMethod.VNPAY.name())
                .message(message)
                .paymentTime(payDate)
                .build();
    }

    private String buildUrlWithEncode(String baseUrl, Map<String, String> params) {

        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey())
                    .append("=")
                    .append(java.net.URLEncoder.encode(entry.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                    .append("&");
        }

        url.deleteCharAt(url.length() - 1);

        return url.toString();
    }

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
