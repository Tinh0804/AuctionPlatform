package com.ecommerce.auctionplatform.payment.application.port.in;

import com.ecommerce.auctionplatform.payment.application.dto.command.PaymentCommand;
import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentCallbackResponse;
import com.ecommerce.auctionplatform.payment.application.dto.response.PaymentResponse;
import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;

import java.util.Map;

public interface PaymentUseCase {
    PaymentResponse createPayment(PaymentCommand command);

    PaymentCallbackResponse processCallback(PaymentMethod method, Map<String, String> callbackData);
}
