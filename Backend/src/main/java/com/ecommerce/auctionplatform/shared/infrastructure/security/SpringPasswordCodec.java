package com.ecommerce.auctionplatform.shared.infrastructure.security;

import com.ecommerce.auctionplatform.shared.application.port.out.PasswordCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringPasswordCodec implements PasswordCodec {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String encode(String rawValue) {
        return passwordEncoder.encode(rawValue);
    }

    @Override
    public boolean matches(String rawValue, String encodedValue) {
        return passwordEncoder.matches(rawValue, encodedValue);
    }
}
