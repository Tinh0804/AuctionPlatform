package com.ecommerce.auctionplatform.identity.infrastructure.security;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {
    private static final String SIGNER_KEY =
            "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Test
    void generatedTokenExposesOnlyFrameworkNeutralVerifiedClaims() {
        JwtTokenProvider provider = new JwtTokenProvider(SIGNER_KEY, 60, 120);
        UUID accountId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        String token = provider.generateAccessToken(
                new TokenProvider.TokenSubject(accountId, profileId, "USER"));
        TokenProvider.VerifiedToken verified = provider.verify(token, false);

        assertEquals(accountId, verified.accountId());
        assertEquals(profileId, verified.profileId());
        assertEquals("USER", verified.scope());
        assertEquals(TokenProvider.TokenType.ACCESS, verified.type());
    }

    @Test
    void invalidTokenIsConvertedToApplicationError() {
        JwtTokenProvider provider = new JwtTokenProvider(SIGNER_KEY, 60, 120);

        assertThrows(AppException.class, () -> provider.verify("not-a-jwt", false));
    }
}
