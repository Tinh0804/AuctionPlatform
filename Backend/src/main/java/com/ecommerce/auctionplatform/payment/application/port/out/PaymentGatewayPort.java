package com.ecommerce.auctionplatform.payment.application.port.out;

import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Port/Out – Abstraction for payment gateway integration.
 * Both VNPayGatewayAdapter and MoMoGatewayAdapter implement this interface.
 * The application layer depends on this interface, NOT on concrete adapters.
 *
 * Benefits:
 * - Easily switch or add payment gateways without changing business logic
 * - Can be mocked in unit tests
 * - Follows Dependency Inversion Principle
 */
public interface PaymentGatewayPort {

    /**
     * Create a payment request and return a redirect URL or QR code.
     */
    GatewayPaymentResult createPayment(GatewayPaymentRequest request);

    GatewayCallbackResult verifyCallback(Map<String, String> callbackData);

    /**
     * Returns the gateway type identifier (e.g. "VNPAY", "MOMO").
     * Used for routing in a multi-gateway setup.
     */
    PaymentMethod gatewayType();

    record GatewayPaymentRequest(
            UUID transactionId,
            BigDecimal amount,
            String orderInfo,
            String returnUrl,
            String notifyUrl
    ) {
    }

    record GatewayPaymentResult(String paymentUrl, String externalOrderId, String message) {
    }

    record GatewayCallbackResult(
            UUID transactionId,
            String externalTransactionId,
            BigDecimal amount,
            boolean signatureValid,
            boolean successful,
            String message,
            String paymentTime,
            String rawPayload
    ) {
    }
}
