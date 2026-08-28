package com.ecommerce.auctionplatform.identity.presentation.rest;

import com.ecommerce.auctionplatform.identity.application.dto.command.AdminUpdateUserCommand;
import com.ecommerce.auctionplatform.identity.application.port.in.AdminUserUseCase;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.AdminUserUpdateRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.presentation.mapper.UserResponseMapper;
import com.ecommerce.auctionplatform.shared.application.model.PageQuery;
import com.ecommerce.auctionplatform.shared.domain.model.PageResult;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserUseCase adminUserUseCase;
    private final UserResponseMapper responseMapper;

    @GetMapping
    public APIResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "name") Pageable pageable
    ) {
        var result = adminUserUseCase.getAllUsers(keyword, new PageQuery(
                        pageable.getPageNumber(), pageable.getPageSize(),
                        pageable.getSort().stream().findFirst().map(order -> order.getProperty()).orElse("name"),
                        pageable.getSort().stream().findFirst().map(order -> order.isAscending()).orElse(true)));
        Pageable responsePageable = PageRequest.of(result.pageNumber(), result.pageSize(), pageable.getSort());
        Page<UserResponse> page = new PageImpl<>(
                result.items().stream().map(responseMapper::toUserResponse).toList(),
                responsePageable,
                result.totalElements());
        return APIResponse.<Page<UserResponse>>builder()
                .message("Users fetched successfully")
                .result(page)
                .build();
    }

    @GetMapping("/{id}")
    public APIResponse<UserResponse> getUserDetail(@PathVariable UUID id) {
        return APIResponse.<UserResponse>builder()
                .message("User detail fetched successfully")
                .result(responseMapper.toUserResponse(adminUserUseCase.getUserDetail(id)))
                .build();
    }

    @PutMapping("/{id}/toggle-active")
    public APIResponse<Void> toggleUserStatus(@PathVariable UUID id) {
        adminUserUseCase.toggleUserStatus(id);
        return APIResponse.<Void>builder().message("User status updated successfully").build();
    }

    @PutMapping("/{id}/wallet/toggle-status")
    public APIResponse<Void> toggleWalletStatus(@PathVariable UUID id) {
        adminUserUseCase.toggleWalletStatus(id);
        return APIResponse.<Void>builder().message("Wallet status updated successfully").build();
    }

    @PutMapping("/{id}/verification")
    public APIResponse<Void> updateVerificationStatus(@PathVariable UUID id, @RequestParam String status) {
        adminUserUseCase.updateVerificationStatus(id, status);
        return APIResponse.<Void>builder().message("Verification status updated successfully").build();
    }

    @PutMapping("/{id}")
    public APIResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid AdminUserUpdateRequest request
    ) {
        var command = new AdminUpdateUserCommand(
                request.name(), request.phone(), request.email(), request.identityCard(),
                request.gender(), request.dob(), request.reputationScore());
        return APIResponse.<UserResponse>builder()
                .message("User updated successfully")
                .result(responseMapper.toUserResponse(adminUserUseCase.updateUser(id, command)))
                .build();
    }

    @DeleteMapping("/{id}")
    public APIResponse<Void> deleteUser(@PathVariable UUID id) {
        adminUserUseCase.deleteUser(id);
        return APIResponse.<Void>builder().message("User deleted successfully").build();
    }
}
