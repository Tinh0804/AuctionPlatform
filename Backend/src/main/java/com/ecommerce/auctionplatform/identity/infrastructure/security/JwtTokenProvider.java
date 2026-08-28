package com.ecommerce.auctionplatform.identity.infrastructure.security;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider {
    private static final String ISSUER = "AuctionPlatform";

    private final String signerKey;
    private final long accessDurationSeconds;
    private final long refreshDurationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.signerKey}") String signerKey,
            @Value("${jwt.valid-duration}") long accessDurationSeconds,
            @Value("${jwt.refreshable-duration}") long refreshDurationSeconds
    ) {
        this.signerKey = signerKey;
        this.accessDurationSeconds = accessDurationSeconds;
        this.refreshDurationSeconds = refreshDurationSeconds;
    }

    @Override
    public String generateAccessToken(TokenSubject subject) {
        return generate(subject, accessDurationSeconds, TokenType.ACCESS);
    }

    @Override
    public String generateRefreshToken(TokenSubject subject) {
        return generate(subject, refreshDurationSeconds, TokenType.REFRESH);
    }

    @Override
    public VerifiedToken verify(String token, boolean ignoreExpiration) {
        try {
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(verifier)) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            Date expiry = claims.getExpirationTime();
            if (expiry == null || !ignoreExpiration && !expiry.after(new Date())) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }
            if (!ISSUER.equals(claims.getIssuer())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            return new VerifiedToken(
                    UUID.fromString(claims.getSubject()),
                    UUID.fromString(claims.getStringClaim("profile_id")),
                    claims.getJWTID(),
                    claims.getStringClaim("scope"),
                    TokenType.valueOf(claims.getStringClaim("token_type")),
                    expiry.toInstant());
        } catch (AppException exception) {
            throw exception;
        } catch (JOSEException | ParseException | IllegalArgumentException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String generate(TokenSubject subject, long durationSeconds, TokenType type) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject.accountId().toString())
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(durationSeconds, ChronoUnit.SECONDS)))
                .claim("scope", subject.scope())
                .claim("profile_id", subject.profileId().toString())
                .claim("token_type", type.name())
                .jwtID(UUID.randomUUID().toString())
                .build();
        SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        try {
            signedJwt.sign(new MACSigner(signerKey));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Unable to sign JWT", exception);
        }
    }
}
