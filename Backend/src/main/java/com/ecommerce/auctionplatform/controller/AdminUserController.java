package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.AdminUserUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.service.UserService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {

    UserService userService;

    @GetMapping
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "account.createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> users = userService.getAllUsers(keyword, pageable);
        return APIResponse.<Page<UserResponse>>builder()
                .result(users)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<UserResponse> getUserDetail(@PathVariable UUID id) {
        return APIResponse.<UserResponse>builder()
                .result(userService.getUserDetail(id))
                .build();
    }

    @PutMapping("/{id}/toggle-active")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> toggleUserStatus(@PathVariable UUID id) {
        userService.toggleUserStatus(id);
        return APIResponse.<Void>builder()
                .message("User status toggled successfully")
                .build();
    }

    @PutMapping("/{id}/wallet/toggle-status")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> toggleWalletStatus(@PathVariable UUID id) {
        userService.toggleWalletStatus(id);
        return APIResponse.<Void>builder()
                .message("Wallet status toggled successfully")
                .build();
    }

    @PutMapping("/{id}/verification")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> updateVerificationStatus(@PathVariable UUID id, @RequestParam String status) {
        userService.updateVerificationStatus(id, status);
        return APIResponse.<Void>builder()
                .message("Verification status updated successfully")
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid AdminUserUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .result(userService.adminUpdateUser(id, request))
                .message("User updated successfully")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public APIResponse<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return APIResponse.<Void>builder()
                .message("User deleted successfully")
                .build();
    }
}
