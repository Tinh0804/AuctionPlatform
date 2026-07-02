package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.PaymentRequest;
import com.ecommerce.auctionplatform.dto.respose.PaymentResponse;
import com.ecommerce.auctionplatform.entity.Transaction;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.entity.enums.TransactionStatus;
import com.ecommerce.auctionplatform.entity.enums.TransactionType;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.TransactionRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
import com.ecommerce.auctionplatform.utils.HmacUtil;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoMoService {

    final WalletRepository walletRepository;
    final UserRepository userRepository;
    final TransactionRepository transactionRepository;

    @Value("${momo.partner-code:}")
    String partnerCode;

    @Value("${momo.access-key:}")
    String accessKey;

    @Value("${momo.secret-key:}")
    String secretKey;

    @Value("${momo.api-url:}")
    String apiUrl;

    @Value("${momo.return-url:}")
    String returnUrl;

    @Value("${momo.notify-url:}")
    String notifyUrl;

    @Value("${momo.request-type:captureWallet}")
    String requestType;

    public PaymentResponse createPayment(PaymentRequest request) {
        User currentUser = getCurrentUser();
        Wallet wallet = walletRepository.findByUser(currentUser)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)); // or WALLET_NOT_FOUND

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(TransactionType.DEPOSIT)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .status(TransactionStatus.PENDING)
                .gatewayProvider("MOMO")
                .referenceId(UUID.randomUUID())
                .note(request.getOrderInfo())
                .build();
        
        transaction = transactionRepository.save(transaction);
        
        String orderId = transaction.getId().toString();
        String amountStr = String.valueOf(request.getAmount().longValue());
        String orderInfo = request.getOrderInfo() != null ? request.getOrderInfo() : "Nap tien vao vi";
        String requestId = UUID.randomUUID().toString();
        String extraData = "";

        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=" + requestType;

        String signature = HmacUtil.calculateHmacSHA256(rawHash, secretKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amountStr);
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", returnUrl);
        requestBody.put("ipnUrl", notifyUrl);
        requestBody.put("lang", "vi");
        requestBody.put("requestType", requestType);
        requestBody.put("autoCapture", true);
        requestBody.put("extraData", extraData);
        requestBody.put("signature", signature);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("payUrl")) {
                String payUrl = (String) responseBody.get("payUrl");
                
                transaction.setGatewayTxId(requestId);
                transactionRepository.save(transaction);

                return PaymentResponse.builder()
                        .status("SUCCESS")
                        .message("Created payment URL successfully")
                        .paymentUrl(payUrl)
                        .orderId(orderId)
                        .transactionId(transaction.getId().toString())
                        .amount(request.getAmount())
                        .paymentMethod("MOMO")
                        .build();
            } else {
                log.error("MoMo API Error: {}", responseBody);
                throw new RuntimeException("Failed to create MoMo payment URL: " + responseBody.get("message"));
            }
        } catch (Exception e) {
            log.error("Error calling MoMo API", e);
            throw new RuntimeException("Failed to connect to MoMo API");
        }
    }

    @Transactional
    public void processCallback(Map<String, Object> callbackData) {
        log.info("Received MoMo IPN Callback: {}", callbackData);
        
        String orderId = (String) callbackData.get("orderId");
        String partnerCodeCb = (String) callbackData.get("partnerCode");
        String accessKeyCb = (String) callbackData.get("accessKey");
        String amount = String.valueOf(callbackData.get("amount"));
        String extraData = (String) callbackData.get("extraData");
        String message = (String) callbackData.get("message");
        String orderInfo = (String) callbackData.get("orderInfo");
        String orderType = (String) callbackData.get("orderType");
        String payType = (String) callbackData.get("payType");
        String requestId = (String) callbackData.get("requestId");
        String responseTime = String.valueOf(callbackData.get("responseTime"));
        String resultCode = String.valueOf(callbackData.get("resultCode"));
        String transId = String.valueOf(callbackData.get("transId"));
        String signature = (String) callbackData.get("signature");

       
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&message=" + message +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&orderType=" + orderType +
                "&partnerCode=" + partnerCode +
                "&payType=" + payType +
                "&requestId=" + requestId +
                "&responseTime=" + responseTime +
                "&resultCode=" + resultCode +
                "&transId=" + transId;

        String expectedSignature = HmacUtil.calculateHmacSHA256(rawHash, secretKey);

        if (!expectedSignature.equals(signature)) {
            log.error("MoMo Callback Signature Validation Failed! Expected: {}, Actual: {}", expectedSignature, signature);
            throw new RuntimeException("Invalid MoMo Signature");
        }

        Transaction transaction = transactionRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new RuntimeException("Transaction not found for ID: " + orderId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transaction {} is already processed (Status: {})", orderId, transaction.getStatus());
            return;
        }

        transaction.setGatewayResponse(callbackData.toString());

        //  Process Result
        if ("0".equals(resultCode)) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            
            Wallet wallet = transaction.getWallet();
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(transaction.getAmount()));
            walletRepository.save(wallet);
            log.info("Successfully added {} to wallet {}", transaction.getAmount(), wallet.getId());
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            log.info("MoMo payment failed for transaction {}. Result code: {}", orderId, resultCode);
        }

        transactionRepository.save(transaction);
    }

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
