package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.AccountRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
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
public class JwtService {


    UserRepository userRepository;
    BlackListService blackListService;
    AccountRepository accountRepository;


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
    public String refreshToken(String refreshToken) throws JOSEException, ParseException {
        String token = SecurityUtils.getCurrentToken()
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));

        SignedJWT signedRefresh = verifyToken(refreshToken, true);
        SignedJWT signedAccess = verifyToken(token, false);

        String refreshSubject = signedRefresh.getJWTClaimsSet().getSubject(); // Thường là username hoặc userId
        String accessSubject = signedAccess.getJWTClaimsSet().getSubject();
        String accessJti = signedAccess.getJWTClaimsSet().getJWTID(); // ID của access token cũ

        if (!refreshSubject.equals(accessSubject)) {
            throw new AppException(ErrorCode.INVALID_TOKEN); // Hoặc INVALID_TOKEN
        }

        Date accessExpiryTime = signedAccess.getJWTClaimsSet().getExpirationTime();
        long remainingTime = accessExpiryTime.getTime() - System.currentTimeMillis();

        if (remainingTime > 0) {
            // Hàm này gọi tới TokenBlacklistService đã viết ở trên
            blackListService.addToBlackList(accessJti, remainingTime);
        }

        Account account = accountRepository.findById(UUID.fromString(accessSubject))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return generateAccessToken(account);

    }

    private String generateToken(Account account,long duration){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        User user = userRepository.findByAccountId(account.getId())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getId().toString())
                .issuer("AuctionPlatform")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS)))
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

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier=new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT=SignedJWT.parse(token);
        boolean verified= signedJWT.verify(verifier);
        Date expiryTime=(isRefresh) ?
                new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(VALID_DURATION,ChronoUnit.SECONDS).toEpochMilli())//kiểm tra token refresh hết hạn
                :signedJWT.getJWTClaimsSet().getExpirationTime();//kiểm tra tokenVerify hết hạn

        if(!(verified && expiryTime.after(new Date())))
            throw  new AppException(ErrorCode.INVALID_TOKEN);

        if(blackListService.isBlackListed(token))
            throw  new AppException(ErrorCode.TOKEN_BLACKLISTED);
        return  signedJWT;
    }

    private String buildScope(Account account) {
        Role role = account.getRole();
        if (role == null || role.getName() == null) {
            return "";
        }
        return role.getName().toUpperCase();   // Chỉ "USER" hoặc "ADMIN"
    }

}
