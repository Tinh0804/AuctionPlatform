package com.ecommerce.auctionplatform.identity.application.port.out;

public interface TokenBlacklist {
    void add(String token, long remainingTimeMillis);
    boolean contains(String token);
}
