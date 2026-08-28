package com.ecommerce.auctionplatform.payment.presentation.dto.request;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class WithdrawRequest {
    @NotBlank
    private String bank;
    @NotBlank
    @JsonProperty("account_number")
    private String account_number;
    @NotNull
    @Positive
    private Long amount;
}
