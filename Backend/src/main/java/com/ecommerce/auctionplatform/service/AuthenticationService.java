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
import com.ecommerce.auctionplatform.dto.request.RegisterRequest;
import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.entity.enums.ProviderType;
import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import com.ecommerce.auctionplatform.repository.RoleRepository;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.auctionplatform.dto.request.RefreshRequest;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {
    AccountRepository accountRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    BlackListService blackListService;

    PasswordEncoder encoder;
    JwtService jwtService;

    AccountMapper accountMapper;

    @Transactional
    public void register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUserName())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        Role customerRole = roleRepository.findByName(PredefinedRole.RoleName.CUSTOMER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        Account account = Account.builder()
                .username(request.getUserName())
                .password(encoder.encode(request.getPassWord()))
                .role(customerRole)
                .isActive(true)
                .provider(ProviderType.LOCAL)
                .build();

        User user = User.builder()
                .name(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .verificationStatus(VerificationStatus.UNVERIFIED)
                .gender(null)
                .account(account)
                .build();

        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest request) {
        Account account = accountRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND));

        if(!encoder.matches(request.getPassWord(), account.getPassword()))
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
    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException {
        return jwtService.refreshToken(request);
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
