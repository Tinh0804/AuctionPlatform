package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.identity.application.dto.command.AdminUpdateUserCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.application.mapper.UserMapper;
import com.ecommerce.auctionplatform.identity.application.port.in.AdminUserUseCase;
import com.ecommerce.auctionplatform.identity.application.port.out.WalletProfilePort;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import com.ecommerce.auctionplatform.identity.domain.valueobject.UserSearchCriteria;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService implements AdminUserUseCase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final WalletProfilePort walletProfilePort;

    @Override
    @Transactional(readOnly = true)
    public PageResult<UserResponse> getAllUsers(String keyword, PageQuery pageQuery) {
        PageResult<User> page = userRepository.searchNonAdmin(new UserSearchCriteria(
                keyword,
                pageQuery.pageNumber(),
                pageQuery.pageSize(),
                pageQuery.sortBy(),
                pageQuery.ascending()));
        return new PageResult<>(
                page.items().stream().map(userMapper::toUserResponse).toList(),
                page.pageNumber(),
                page.pageSize(),
                page.totalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserDetail(UUID id) {
        UserResponse response = userMapper.toUserResponse(user(id));
        response.setWallet(walletProfilePort.getWallet(id));
        return response;
    }

    @Override
    @Transactional
    public void toggleUserStatus(UUID id) {
        User user = mutableNonAdmin(id);
        user.toggleAccountStatus();
        userRepository.save(user);
    }

    @Override
    public void toggleWalletStatus(UUID id) {
        mutableNonAdmin(id);
        walletProfilePort.toggleStatus(id);
    }

    @Override
    @Transactional
    public void updateVerificationStatus(UUID id, String status) {
        User user = mutableNonAdmin(id);
        try {
            user.updateVerificationStatus(VerificationStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        } catch (RuntimeException exception) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, AdminUpdateUserCommand command) {
        User user = mutableNonAdmin(id);
        user.updateByAdmin(
                command.name(), command.phone(), command.email(), command.identityCard(),
                command.gender(), command.dob(), command.reputationScore());
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = mutableNonAdmin(id);
        user.anonymizeForDeletion();
        userRepository.save(user);
    }

    private User mutableNonAdmin(UUID id) {
        User user = user(id);
        if (user.getAccount() != null
                && user.getAccount().getRole() != null
                && PredefinedRole.RoleName.ADMIN.equals(user.getAccount().getRole().getName())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    private User user(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
