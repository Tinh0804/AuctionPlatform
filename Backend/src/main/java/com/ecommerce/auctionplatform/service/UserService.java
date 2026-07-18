package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.AddressRequest;
import com.ecommerce.auctionplatform.dto.request.AdminUserUpdateRequest;
import com.ecommerce.auctionplatform.dto.request.PhoneUpdateRequest;
import com.ecommerce.auctionplatform.dto.request.UserUpdateRequest;
import com.ecommerce.auctionplatform.dto.respose.AddressDto;
import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.dto.respose.WalletResponse;
import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.entity.Address;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import com.ecommerce.auctionplatform.entity.enums.WalletStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.mapper.UserMapper;
import com.ecommerce.auctionplatform.mapper.WalletMapper;
import com.ecommerce.auctionplatform.repository.AccountRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    UserRepository userRepository;
    AccountRepository accountRepository;
    UserMapper userMapper;
    WalletMapper walletMapper;
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

    // --- Admin Methods ---

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public Page<UserResponse> getAllUsers(String keyword, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.notEqual(root.get("account").get("role").get("name"), "ADMIN"));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                Predicate phonePredicate = cb.like(cb.lower(root.get("phone")), searchPattern);
                Predicate usernamePredicate = cb.like(cb.lower(root.get("account").get("username")), searchPattern);
                
                predicates.add(cb.or(namePredicate, emailPredicate, phonePredicate, usernamePredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(userMapper::toUserResponse);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public UserResponse getUserDetail(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        UserResponse response = userMapper.toUserResponse(user);
        
        walletRepository.findByUser(user).ifPresent(wallet -> {
            response.setWallet(walletMapper.toWalletResponse(wallet));
        });
        
        return response;
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void toggleUserStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        if (PredefinedRole.ADMIN.name().equals(user.getAccount().getRole().getName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        Account account = user.getAccount();
        account.setIsActive(!account.getIsActive());
        accountRepository.save(account);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void toggleWalletStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (wallet.getStatus() == WalletStatus.ACTIVE) {
            wallet.setStatus(WalletStatus.FROZEN);
        } else {
            wallet.setStatus(WalletStatus.ACTIVE);
        }
        walletRepository.save(wallet);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void updateVerificationStatus(UUID id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                
        try {
            VerificationStatus newStatus = VerificationStatus.valueOf(status.toUpperCase());
            user.setVerificationStatus(newStatus);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public UserResponse adminUpdateUser(UUID id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            user.setEmail(request.getEmail());
        }
        if (request.getIdentityCard() != null) {
            user.setIdentityCard(request.getIdentityCard().trim().isEmpty() ? null : request.getIdentityCard());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getDob() != null) {
            user.setDob(request.getDob());
        }
        if (request.getReputationScore() != null) {
            user.setReputationScore(request.getReputationScore());
        }

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize(PredefinedRole.HAS_ROLE_ADMIN)
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        userRepository.delete(user);
    }
}
