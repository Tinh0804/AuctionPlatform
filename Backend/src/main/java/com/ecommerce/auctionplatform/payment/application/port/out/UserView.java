package com.ecommerce.auctionplatform.payment.application.port.out;

import java.util.UUID;

public record UserView(UUID id, String name, String phone, Integer reputationScore) {
}
