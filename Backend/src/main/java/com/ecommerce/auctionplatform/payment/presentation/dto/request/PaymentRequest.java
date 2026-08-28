package com.ecommerce.auctionplatform.payment.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    @NotBlank(message = "Reference ID không được để trống")
    String referenceId;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 10000, message = "Số tiền tối thiểu 10,000 VND")
    Double amount;

    @NotBlank(message = "Mô tả không được để trống")
    String orderInfo;

    String method;

    String returnUrl;

    String notifyUrl;
}
