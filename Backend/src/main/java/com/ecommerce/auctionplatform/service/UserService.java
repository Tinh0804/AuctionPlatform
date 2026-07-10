package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.AddressRequest;
import com.ecommerce.auctionplatform.dto.request.PhoneUpdateRequest;
import com.ecommerce.auctionplatform.dto.request.UserUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.AddressDto;
import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.dto.respose.WalletResponse;
import com.ecommerce.auctionplatform.entity.Address;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.entity.enums.WalletStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.mapper.UserMapper;
import com.ecommerce.auctionplatform.repository.AddressRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    WalletRepository walletRepository;
    AddressRepository addressRepository;
    CloudinaryService cloudinaryService;

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponse getUserInfo() {
        User user = getCurrentUser();
        
        UserResponse userResponse = userMapper.toUserResponse(user);
        
        // Find or auto-create wallet
        Wallet wallet = walletRepository.findByUser(user).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .availableBalance(BigDecimal.ZERO)
                    .frozenBalance(BigDecimal.ZERO)
                    .status(WalletStatus.ACTIVE)
                    .build();
            return walletRepository.save(newWallet);
        });

        userResponse.setWallet(WalletResponse.builder()
                .id(wallet.getId())
                .availableBalance(wallet.getAvailableBalance())
                .frozenBalance(wallet.getFrozenBalance())
                .hasPin(wallet.getPinCode() != null && !wallet.getPinCode().trim().isEmpty())
                .status(wallet.getStatus().name())
                .build());

        List<Address> addresses = addressRepository.findByUserId(user.getId());
        userResponse.setAddresses(addresses.stream().map(this::mapToAddressDto).collect(Collectors.toList()));

        return userResponse;
    }

    public UserResponse updateUserInfo(UserUpdateRequest request) {
        User user = getCurrentUser();
        if (request.getName() != null) user.setName(request.getName());
        if (request.getDob() != null) user.setDob(request.getDob());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getAvatarImage() != null) user.setAvatarImage(request.getAvatarImage());
        userRepository.save(user);
        return getUserInfo();
    }

    public UserResponse updateAvatar(org.springframework.web.multipart.MultipartFile file) {
        User user = getCurrentUser();
        try {
            String avatarUrl = cloudinaryService.uploadFile(file, "auction_project/avatars", new java.util.HashMap<>());
            user.setAvatarImage(avatarUrl);
            userRepository.save(user);
        } catch (java.io.IOException e) {
            log.error("Failed to upload avatar", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return getUserInfo();
    }

    public UserResponse updatePhone(PhoneUpdateRequest request) {
        String firebasePhone = verifyFirebaseTokenAndGetPhone(request.getFirebaseIdToken());
        if (firebasePhone == null || firebasePhone.trim().isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHENTACATED);
        }
        User user = getCurrentUser();
        user.setPhone(firebasePhone);
        userRepository.save(user);
        return getUserInfo();
    }

    public AddressDto addAddress(AddressRequest request) {
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
        return mapToAddressDto(addressRepository.save(address));
    }

    public AddressDto updateAddress(UUID addressId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException(ErrorCode.AUCTION_NOT_FOUND)); // Or a better error
        
        if (request.getIsDefault() != null && request.getIsDefault()) {
            resetDefaultAddresses(address.getUser().getId());
        }

        address.setWard(request.getWard());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setAddressLine(request.getAddressLine());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }
        return mapToAddressDto(addressRepository.save(address));
    }


    public void deleteAddress(UUID addressId) {
        addressRepository.deleteById(addressId);
    }

    private void resetDefaultAddresses(UUID userId) {
        List<Address> addresses = addressRepository.findByUserId(userId);
        for (Address addr : addresses) {
            if (addr.getIsDefault()) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

    private AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .addressLine(address.getAddressLine())
                .isDefault(address.getIsDefault())
                .build();
    }

    private String verifyFirebaseTokenAndGetPhone(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return (String) decodedToken.getClaims().get("phone_number");
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed via Admin SDK: {}", e.getMessage());
            return null;
        }
    }

    public boolean isCreatedAuction(){
        User user = getCurrentUser();
        return user.getVerificationStatus()!= null && user.getVerificationStatus().name().equals("VERIFIED") && user.getReputationScore()>=50;
    }
}
