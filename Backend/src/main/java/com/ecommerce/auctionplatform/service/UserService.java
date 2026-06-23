package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.respose.UserResponse;
import com.ecommerce.auctionplatform.dto.respose.WalletResponse;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.entity.enums.WalletStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.mapper.UserMapper;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    WalletRepository walletRepository;

    public UserResponse getUserInfo() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        User user = userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
        
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

        return userResponse;
    }



}
