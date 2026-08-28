package com.ecommerce.auctionplatform.identity.presentation.rest;

import com.ecommerce.auctionplatform.identity.application.dto.command.UpsertAddressCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdatePhoneCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdateUserCommand;
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.identity.application.port.in.UserUseCase;
import com.ecommerce.auctionplatform.shared.presentation.mapper.FileUploadMapper;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.AddressRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.PhoneUpdateRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.request.UserUpdateRequest;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.AddressResponse;
import com.ecommerce.auctionplatform.identity.presentation.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.presentation.mapper.UserResponseMapper;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequestMapping("/users")
public class UserController {

    UserUseCase userService;
    UserResponseMapper responseMapper;

    @GetMapping("/my-info")
    public APIResponse<UserResponse> getMyInfo() {
        return APIResponse.<UserResponse>builder()
                .message("User information retrieved successfully")
                .result(responseMapper.toUserResponse(userService.getUserInfo()))
                .build();
    }

    @PutMapping("/my-info")
    public APIResponse<UserResponse> updateMyInfo(@Valid @RequestBody UserUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .message("User information updated successfully")
                .result(responseMapper.toUserResponse(
                        userService.updateUserInfo(UpdateUserCommand.builder()
                                .name(request.name())
                                .email(request.email())
                                .gender(request.gender())
                                .dob(request.dob())
                                .avatarImage(request.avatarImage())
                                .build())))
                .build();
    }

    @PostMapping(value = "/my-info/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public APIResponse<UserResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return APIResponse.<UserResponse>builder()
                .message("Avatar updated successfully")
                .result(responseMapper.toUserResponse(
                        userService.updateAvatar(FileUploadMapper.toContent(file))))
                .build();
    }

    @PostMapping("/my-info/phone")
    public APIResponse<UserResponse> updatePhone(@Valid @RequestBody PhoneUpdateRequest request) {
        return APIResponse.<UserResponse>builder()
                .message("Phone number updated successfully")
                .result(responseMapper.toUserResponse(
                        userService.updatePhone(UpdatePhoneCommand.builder()
                                .firebaseIdToken(request.firebaseIdToken())
                                .build())))
                .build();
    }

    @PostMapping("/my-info/addresses")
    public APIResponse<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        return APIResponse.<AddressResponse>builder()
                .message("Address added successfully")
                .result(responseMapper.toAddressResponse(userService.addAddress(toCommand(request))))
                .build();
    }

    @PutMapping("/my-info/addresses/{id}")
    public APIResponse<AddressResponse> updateAddress(@PathVariable UUID id, @Valid @RequestBody AddressRequest request) {
        return APIResponse.<AddressResponse>builder()
                .message("Address updated successfully")
                .result(responseMapper.toAddressResponse(userService.updateAddress(id, toCommand(request))))
                .build();
    }

    @DeleteMapping("/my-info/addresses/{id}")
    public APIResponse<Void> deleteAddress(@PathVariable UUID id) {
        userService.deleteAddress(id);
        return APIResponse.<Void>builder()
                .message("Address deleted successfully")
                .build();
    }

    private UpsertAddressCommand toCommand(AddressRequest request) {
        return UpsertAddressCommand.builder()
                .ward(request.ward())
                .district(request.district())
                .city(request.city())
                .addressLine(request.addressLine())
                .isDefault(request.isDefault())
                .build();
    }
}
