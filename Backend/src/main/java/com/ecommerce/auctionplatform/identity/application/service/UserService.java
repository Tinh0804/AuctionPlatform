package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.identity.application.dto.command.UpsertAddressCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdatePhoneCommand;
import com.ecommerce.auctionplatform.identity.application.dto.command.UpdateUserCommand;
import com.ecommerce.auctionplatform.identity.application.dto.response.AddressResponse;
import com.ecommerce.auctionplatform.identity.application.dto.response.UserResponse;
import com.ecommerce.auctionplatform.identity.application.port.out.WalletProfilePort;
import com.ecommerce.auctionplatform.shared.application.port.out.PhoneVerificationPort;
import com.ecommerce.auctionplatform.identity.domain.model.Address;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.exception.FileStorageException;
import com.ecommerce.auctionplatform.identity.application.mapper.UserMapper;
import com.ecommerce.auctionplatform.identity.domain.repository.AddressRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import com.ecommerce.auctionplatform.shared.application.port.out.FileStoragePort;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.ecommerce.auctionplatform.identity.application.port.in.UserUseCase;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService implements UserUseCase {
    UserRepository userRepository;
    UserMapper userMapper;
    WalletProfilePort walletProfilePort;
    AddressRepository addressRepository;
    FileStoragePort cloudinaryService;
    CurrentUserProvider currentUserProvider;
    PhoneVerificationPort phoneVerificationPort;

    private User getCurrentUser() {
        UUID userProfileId = currentUserProvider.currentProfileId().orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHORIZED));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponse getUserInfo() {
        User user = getCurrentUser();
        
        UserResponse userResponse = userMapper.toUserResponse(user);
        
        userResponse.setWallet(walletProfilePort.getWallet(user.getId()));

        List<Address> addresses = addressRepository.findByUserId(user.getId());
        userResponse.setAddresses(addresses.stream().map(this::mapToAddressResponse).collect(Collectors.toList()));

        return userResponse;
    }

    public UserResponse updateUserInfo(UpdateUserCommand request) {
        User user = getCurrentUser();
        user.updateProfile(request.getName(), request.getDob(), request.getGender(), request.getEmail(), request.getAvatarImage());
        userRepository.save(user);
        return getUserInfo();
    }

    public UserResponse updateAvatar(FileContent file) {
        User user = getCurrentUser();
        try {
            String avatarUrl = cloudinaryService.uploadFile(file, "auction_project/avatars", new java.util.HashMap<>());
            user.updateAvatar(avatarUrl);
            userRepository.save(user);
        } catch (FileStorageException e) {
            log.error("Failed to upload avatar", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return getUserInfo();
    }

    public UserResponse updatePhone(UpdatePhoneCommand request) {
        String firebasePhone = phoneVerificationPort.verifiedPhoneNumber(request.getFirebaseIdToken())
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));
        User user = getCurrentUser();
        user.updatePhone(firebasePhone);
        userRepository.save(user);
        return getUserInfo();
    }

    public AddressResponse addAddress(UpsertAddressCommand request) {
        User user = getCurrentUser();
        if (request.getIsDefault() != null && request.getIsDefault()) {
            resetDefaultAddresses(user.getId());
        }
        Address address = Address.builder()
                .user(user)
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .addressLine(request.getAddressLine())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
        return mapToAddressResponse(addressRepository.save(address));
    }

    public AddressResponse updateAddress(UUID addressId, UpsertAddressCommand request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        
        if (request.getIsDefault() != null && request.getIsDefault()) {
            resetDefaultAddresses(address.getUser().getId());
        }

        address.update(request.getWard(), request.getDistrict(), request.getCity(),
                request.getAddressLine(), request.getIsDefault());
        return mapToAddressResponse(addressRepository.save(address));
    }


    public void deleteAddress(UUID addressId) {
        addressRepository.deleteById(addressId);
    }

    private void resetDefaultAddresses(UUID userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address addr : addresses) {
            if (addr.getIsDefault()) {
                addr.clearDefault();
                addressRepository.save(addr);
            }
        }
    }

    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .addressLine(address.getAddressLine())
                .isDefault(address.getIsDefault())
                .build();
    }

    public boolean isCreatedAuction(){
        User user = getCurrentUser();
        return user.getVerificationStatus()!= null && user.getVerificationStatus().name().equals("VERIFIED") && user.getReputationScore()>=50;
    }
}
