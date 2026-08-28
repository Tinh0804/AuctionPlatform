package com.ecommerce.auctionplatform.identity.application.port.out;

import java.time.Instant;
import java.util.UUID;

/** Framework-neutral token cryptography port. */
public interface TokenProvider {
    String generateAccessToken(TokenSubject subject);

    String generateRefreshToken(TokenSubject subject);

    VerifiedToken verify(String token, boolean ignoreExpiration);

    record TokenSubject(UUID accountId, UUID profileId, String scope) {
    }

    record VerifiedToken(
            UUID accountId,
            UUID profileId,
            String tokenId,
            String scope,
            TokenType type,
            Instant expiresAt
    ) {
    }

    enum TokenType {
        ACCESS,
        REFRESH
    }
}
