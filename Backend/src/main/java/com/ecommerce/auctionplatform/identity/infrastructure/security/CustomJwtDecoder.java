package com.ecommerce.auctionplatform.identity.infrastructure.security;

import com.ecommerce.auctionplatform.identity.application.port.out.TokenBlacklist;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    private final String signerKey;
    private final TokenBlacklist tokenBlacklist;
    private NimbusJwtDecoder nimbusJwtDecoder;

    public CustomJwtDecoder(
            @Value("${jwt.signerKey}") String signerKey,
            TokenBlacklist tokenBlacklist
    ) {
        this.signerKey = signerKey;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostConstruct
    public void init() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        this.nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        if (token == null || token.isBlank() || tokenBlacklist.contains(token)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token", ErrorCode.INVALID_TOKEN.getMessage(), null)
            );
        }

        return nimbusJwtDecoder.decode(token);
    }
}

