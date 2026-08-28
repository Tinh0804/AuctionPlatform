package com.ecommerce.auctionplatform.shared.application.port.out;

public interface PasswordCodec {
    String encode(String rawValue);

    boolean matches(String rawValue, String encodedValue);
}
