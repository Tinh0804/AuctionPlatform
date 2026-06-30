package com.ecommerce.auctionplatform.service;

import com.ecommerce.auctionplatform.dto.request.PinSetupRequest;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.Wallet;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.repository.WalletRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {
    WalletRepository walletRepository;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    @Transactional
    public void setupPin(PinSetupRequest request) {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED)));

        User user = userRepository.findById(userProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String firebasePhone = verifyFirebaseTokenAndGetPhone(request.getFirebaseIdToken());

        if (firebasePhone == null || firebasePhone.trim().isEmpty()) {
            log.error("Firebase token does not contain a verified phone number");
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        String userPhone = user.getPhone();
        if (!normalizePhone(firebasePhone).equals(normalizePhone(userPhone))) {
            log.error("Phone number mismatch: Firebase phone = {}, Registered user phone = {}", firebasePhone, userPhone);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        Wallet wallet = walletRepository.findByUser(user).orElseGet(() -> {
            Wallet newWallet = Wallet.builder()
                    .user(user)
                    .availableBalance(java.math.BigDecimal.ZERO)
                    .frozenBalance(java.math.BigDecimal.ZERO)
                    .status(com.ecommerce.auctionplatform.entity.enums.WalletStatus.ACTIVE)
                    .build();
            return walletRepository.save(newWallet);
        });

        wallet.setPinCode(passwordEncoder.encode(request.getNewPin()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        log.info("Wallet PIN successfully configured for user {}", userProfileId);
    }

    private User getCurrentUser() {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(()->
                new AppException(ErrorCode.UNAUTHORIZED)));
        return userRepository.findById(userProfileId).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String verifyFirebaseTokenAndGetPhone(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            return (String) decodedToken.getClaims().get("phone_number");
        } catch (Exception e) {
            log.error("Firebase token verification failed via Admin SDK: {}", e.getMessage());
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String normalizePhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", ""); // keep only digits
        if (digits.length() >= 9) {
            return digits.substring(digits.length() - 9);
        }
        return digits;
    }
}
