package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.LoginRequest;
import com.ecommerce.auctionplatform.dto.respose.AuthenticationResponse;
import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.mapper.AccountMapper;
import com.ecommerce.auctionplatform.repository.AccountRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import com.nimbusds.jose.JOSEException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Service
public class AuthenticationService {
    AccountRepository accountRepository;
    UserRepository userRepository;
    BlackListService blackListService;
    PasswordEncoder encoder;
    JwtService jwtService;

    AccountMapper accountMapper;



    public AuthenticationResponse login(LoginRequest request) {
        Account account = accountRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        if(!encoder.matches(account.getPassword(), request.getPassWord()))
            throw new AppException(ErrorCode.UNAUTHENTACATED);

        if(!account.getIsActive())
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);

        String token = jwtService.generateAccessToken(account);
        String refreshToken = jwtService.generateRefreshToken(account);

        return AuthenticationResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .account(accountMapper.toAccountResponse(account))
                .build();

    }
    public void logout(String refreshToken) {
        String token = SecurityUtils.getCurrentToken().orElseThrow(()->new AppException(ErrorCode.TOKEN_NOT_FOUND));
        blackListService.addToBlackList(refreshToken,99999999L);
        blackListService.addToBlackList(token,99999999L);
    }

    public boolean introspect(String token) throws JOSEException, ParseException {
        try {
            jwtService.verifyToken(token, false);
            return true;
        } catch (AppException e) {
            return false;
        }
    }
}
