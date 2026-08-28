package com.ecommerce.auctionplatform.identity.application.port.in;

import com.ecommerce.auctionplatform.identity.application.dto.command.AdminUpdateUserCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;

import java.util.UUID;

public interface AdminUserUseCase {
    PageResult<UserResponse> getAllUsers(String keyword, PageQuery pageQuery);

    UserResponse getUserDetail(UUID id);

    void toggleUserStatus(UUID id);

    void toggleWalletStatus(UUID id);

    void updateVerificationStatus(UUID id, String status);

    UserResponse updateUser(UUID id, AdminUpdateUserCommand command);

    void deleteUser(UUID id);
}
