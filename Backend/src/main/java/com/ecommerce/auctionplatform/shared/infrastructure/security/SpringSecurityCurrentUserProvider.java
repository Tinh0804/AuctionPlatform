package com.ecommerce.auctionplatform.shared.infrastructure.security;

import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class SpringSecurityCurrentUserProvider implements CurrentUserProvider {
    @Override
    public Optional<UUID> currentProfileId() {
        return claim("profile_id").map(UUID::fromString);
    }

    @Override
    public Optional<UUID> currentAccountId() {
        return jwt().map(Jwt::getSubject).map(UUID::fromString);
    }

    @Override
    public Optional<String> currentRole() {
        return claim("scope");
    }

    @Override
    public Optional<String> currentToken() {
        return jwt().map(Jwt::getTokenValue);
    }

    private Optional<String> claim(String name) {
        return jwt().map(value -> value.getClaimAsString(name));
    }

    private Optional<Jwt> jwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt value) {
            return Optional.of(value);
        }
        return Optional.empty();
    }
}
