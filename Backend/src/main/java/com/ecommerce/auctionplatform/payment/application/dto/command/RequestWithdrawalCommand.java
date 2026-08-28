package com.ecommerce.auctionplatform.payment.application.dto.command;

import java.math.BigDecimal;

public record RequestWithdrawalCommand(String bank, String accountNumber, BigDecimal amount) {
}
