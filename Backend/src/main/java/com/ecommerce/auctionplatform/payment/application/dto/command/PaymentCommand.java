package com.ecommerce.auctionplatform.payment.application.dto.command;

import com.ecommerce.auctionplatform.payment.domain.enums.PaymentMethod;
import lombok.Builder;

@Builder
public record PaymentCommand(
        String referenceId,
        Double amount,
        String orderInfo,
        PaymentMethod method,
        String returnUrl,
        String notifyUrl
) {
    public String getReferenceId() { return referenceId; }
    public Double getAmount() { return amount; }
    public String getOrderInfo() { return orderInfo; }
    public PaymentMethod getMethod() { return method; }
    public String getReturnUrl() { return returnUrl; }
    public String getNotifyUrl() { return notifyUrl; }
}
