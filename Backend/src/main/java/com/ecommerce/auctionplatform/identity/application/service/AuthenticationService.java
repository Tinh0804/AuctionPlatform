package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.port.out.PasswordCodec;
import com.ecommerce.auctionplatform.identity.application.dto.command.LoginCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RefreshTokenCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.RegisterCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.AuthenticationResponse;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.application.mapper.AccountMapper;
import com.ecommerce.auctionplatform.identity.application.mapper.UserMapper;
import com.ecommerce.auctionplatform.identity.application.port.in.AuthUseCase;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenBlacklist;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider.TokenSubject;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider.TokenType;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenProvider.VerifiedToken;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import com.ecommerce.auctionplatform.identity.domain.model.Account;
import com.ecommerce.auctionplatform.identity.domain.model.Role;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.AccountRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.RoleRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthUseCase {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenBlacklist tokenBlacklist;
    private final PasswordCodec passwordCodec;
    private final TokenProvider tokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public UserResponse register(RegisterCommand request) {
        if (accountRepository.existsByUsername(request.getUserName())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_EXISTED);
        }

        Role customerRole = roleRepository.findByName(PredefinedRole.RoleName.USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Account account = Account.builder()
                .username(request.getUserName())
                .password(passwordCodec.encode(request.getPassWord()))
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
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginCommand request) {
        Account account = accountRepository.findByUsername(request.getUserName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_OR_PASSWORD_INCORRECT));
        if (!passwordCodec.matches(request.getPassWord(), account.getPassword())) {
            throw new AppException(ErrorCode.USER_OR_PASSWORD_INCORRECT);
        }
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new AppException(ErrorCode.ACCOUNT_INACTIVE);
        }
        User user = userRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return authenticationResponse(account, user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(RefreshTokenCommand request) {
        if (request == null || request.getToken() == null || request.getRefreshToken() == null
                || tokenBlacklist.contains(request.getToken())
                || tokenBlacklist.contains(request.getRefreshToken())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        VerifiedToken refresh = tokenProvider.verify(request.getRefreshToken(), false);
        VerifiedToken access = tokenProvider.verify(request.getToken(), true);
        if (refresh.type() != TokenType.REFRESH
                || access.type() != TokenType.ACCESS
                || !refresh.accountId().equals(access.accountId())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        blacklistWhileAlive(request.getToken(), access.expiresAt());
        blacklistWhileAlive(request.getRefreshToken(), refresh.expiresAt());
        Account account = accountRepository.findById(access.accountId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User user = userRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return authenticationResponse(account, user);
    }

    @Override
    public void logout(String refreshToken) {
        String accessToken = currentUserProvider.currentToken()
                .orElseThrow(() -> new AppException(ErrorCode.TOKEN_NOT_FOUND));
        blacklistToken(accessToken, true);
        if (refreshToken != null && !refreshToken.isBlank()) {
            blacklistToken(refreshToken, false);
        }
    }

    @Override
    public boolean introspect(String token) {
        if (token == null || token.isBlank() || tokenBlacklist.contains(token)) {
            return false;
        }
        try {
            return tokenProvider.verify(token, false).type() == TokenType.ACCESS;
        } catch (AppException exception) {
            return false;
        }
    }

    private AuthenticationResponse authenticationResponse(Account account, User user) {
        TokenSubject subject = new TokenSubject(
                account.getId(),
                user.getId(),
                account.getRole() == null ? "" : account.getRole().getName().toUpperCase());
        return AuthenticationResponse.builder()
                .token(tokenProvider.generateAccessToken(subject))
                .refreshToken(tokenProvider.generateRefreshToken(subject))
                .account(accountMapper.toAccountResponse(account))
                .build();
    }

    private void blacklistToken(String token, boolean allowExpired) {
        try {
            VerifiedToken verified = tokenProvider.verify(token, allowExpired);
            blacklistWhileAlive(token, verified.expiresAt());
        } catch (AppException ignored) {
            // Logout remains idempotent for already-invalid client tokens.
        }
    }

    private void blacklistWhileAlive(String token, Instant expiresAt) {
        long remainingMillis = Duration.between(Instant.now(), expiresAt).toMillis();
        if (remainingMillis > 0) {
            tokenBlacklist.add(token, remainingMillis);
        }
    }
}
