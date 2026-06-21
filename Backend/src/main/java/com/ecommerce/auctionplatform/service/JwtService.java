package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.RefreshRequest;
import com.ecommerce.auctionplatform.dto.respose.AuthenticationResponse;
import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.mapper.AccountMapper;
import com.ecommerce.auctionplatform.repository.AccountRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class JwtService {


    UserRepository userRepository;
    BlackListService blackListService;
    AccountRepository accountRepository;


    AccountMapper accountMapper;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected  String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;



    public boolean introspect(String token) throws JOSEException, ParseException {
        try {
            verifyToken(token, false);
            return true;
        } catch (AppException e) {
            return false;
        }
    }
    public String generateAccessToken(Account account){
        return generateToken(account, VALID_DURATION);
    }
    public String generateRefreshToken(Account account){
        return generateToken(account, REFRESHABLE_DURATION);
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException {
        SignedJWT signedRefresh = verifyToken(request.getRefreshToken(), false);
        SignedJWT signedAccess = verifyToken(request.getToken(), true);

        String refreshSubject = signedRefresh.getJWTClaimsSet().getSubject();
        String accessSubject = signedAccess.getJWTClaimsSet().getSubject();
        String accessJti = signedAccess.getJWTClaimsSet().getJWTID();
        String refreshJti = signedRefresh.getJWTClaimsSet().getJWTID();

        if (!refreshSubject.equals(accessSubject)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        java.util.Date accessExpiryTime = signedAccess.getJWTClaimsSet().getExpirationTime();
        long remainingTime = accessExpiryTime.getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            blackListService.addToBlackList(accessJti, remainingTime);
        }

        java.util.Date refreshExpiryTime = signedRefresh.getJWTClaimsSet().getExpirationTime();
        long refreshRemainingTime = refreshExpiryTime.getTime() - System.currentTimeMillis();
        if (refreshRemainingTime > 0) {
            blackListService.addToBlackList(refreshJti, refreshRemainingTime);
        }

        Account account = accountRepository.findById(UUID.fromString(accessSubject))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newToken = generateAccessToken(account);
        String newRefreshToken = generateRefreshToken(account);

        return AuthenticationResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .account(accountMapper.toAccountResponse(account))
                .build();
    }


    private String generateToken(Account account,long duration){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        User user = userRepository.findByAccountId(account.getId())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getId().toString())
                .issuer("AuctionPlatform")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(duration, ChronoUnit.SECONDS)))
                .claim("scope",buildScope(account))
                .claim("profile_id",user.getId())
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject=new JWSObject(header,payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }


    }

    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            return signedJWT.getJWTClaimsSet().getSubject();

        } catch (ParseException e) {
            throw new RuntimeException("Invalid token format");
        }
    }

    public SignedJWT verifyToken(String token, boolean ignoreExpiration) throws JOSEException, ParseException {
        JWSVerifier verifier=new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT=SignedJWT.parse(token);
        boolean verified= signedJWT.verify(verifier);
        Date expiryTime=signedJWT.getJWTClaimsSet().getExpirationTime();

        if(!verified)
            throw new AppException(ErrorCode.INVALID_TOKEN);

        if(!expiryTime.after(new Date()) && !ignoreExpiration)
            throw new AppException(ErrorCode.TOKEN_EXPIRED);

        if(blackListService.isBlackListed(token))
            throw new AppException(ErrorCode.TOKEN_BLACKLISTED);
        return signedJWT;
    }

    private String buildScope(Account account) {
        Role role = account.getRole();
        if (role == null || role.getName() == null) {
            return "";
        }
        return role.getName().toUpperCase();   // Chỉ "USER" hoặc "ADMIN"
    }

}
