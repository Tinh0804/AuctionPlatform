package com.ecommerce.auctionplatform.dto.request;

import lombok.Data;

@Data
public class WithdrawRequest {
    private String bank;
    private String account_number;
    private Long amount;
}
