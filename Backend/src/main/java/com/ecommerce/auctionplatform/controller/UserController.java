package com.ecommerce.auctionplatform.controller;

import com.ecommerce.auctionplatform.dto.request.AddressRequest;
import com.ecommerce.auctionplatform.dto.request.PhoneUpdateRequest;
import com.ecommerce.auctionplatform.dto.request.UserUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.APIResponse;
import com.ecommerce.auctionplatform.dto.respose.AddressDto;
import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.service.UserService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/users")
public class UserController {

    UserService userService;

    @GetMapping("/my-info")
    public APIResponse<UserResponse> getMyInfo() {
        return APIResponse.<UserResponse>builder()
                .message("User information retrieved successfully")
                .result(userService.getUserInfo())
                .build();
    }

    @PutMapping("/my-info")
    public APIResponse<UserResponse> updateMyInfo(@RequestBody UserUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .message("User information updated successfully")
                .result(userService.updateUserInfo(request))
                .build();
    }

    @PostMapping("/my-info/phone")
    public APIResponse<UserResponse> updatePhone(@RequestBody PhoneUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .message("Phone number updated successfully")
                .result(userService.updatePhone(request))
                .build();
    }

    @PostMapping("/my-info/addresses")
    public APIResponse<AddressDto> addAddress(@RequestBody AddressRequest request) {
        return APIResponse.<AddressDto>builder()
                .message("Address added successfully")
                .result(userService.addAddress(request))
                .build();
    }

    @PutMapping("/my-info/addresses/{id}")
    public APIResponse<AddressDto> updateAddress(@PathVariable UUID id, @RequestBody AddressRequest request) {
        return APIResponse.<AddressDto>builder()
                .message("Address updated successfully")
                .result(userService.updateAddress(id, request))
                .build();
    }

    @DeleteMapping("/my-info/addresses/{id}")
    public APIResponse<Void> deleteAddress(@PathVariable UUID id) {
        userService.deleteAddress(id);
        return APIResponse.<Void>builder()
                .message("Address deleted successfully")
                .build();
    }
}
