package com.ecommerce.auctionplatform.identity.application.service;
 
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import com.ecommerce.auctionplatform.shared.application.port.out.FileStoragePort;
import com.ecommerce.auctionplatform.shared.application.port.out.CurrentUserProvider;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.exception.FileStorageException;
import com.ecommerce.auctionplatform.identity.application.port.in.EKycUseCase;
import com.ecommerce.auctionplatform.identity.application.port.out.KycIdentity;
import com.ecommerce.auctionplatform.identity.application.port.out.KycVerificationPort;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
 
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EKycService implements EKycUseCase {
 
    final UserRepository userRepository;
    final FileStoragePort cloudinaryService;
    final CurrentUserProvider currentUserProvider;
    final KycVerificationPort kycVerificationPort;
 
    public void verifyKyc(FileContent frontImage, FileContent backImage) {
        UUID userProfileId = currentUserProvider.currentProfileId().orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHORIZED));
 
        User user = userRepository.findById(userProfileId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
 
        KycIdentity identity = kycVerificationPort.verify(frontImage);
        if (identity.idCard() == null || identity.idCard().isBlank()) {
            throw new AppException(ErrorCode.EKYC_ID_NOT_FOUND);
        }
 
        // 3. Upload 2 file lên Cloudinary dạng bảo mật (type = authenticated)
        String frontUrl;
        String backUrl;
        try {
            Map<String, Object> extraOptions = new HashMap<>();
            extraOptions.put("type", "private");
 
            frontUrl = cloudinaryService.uploadFile(frontImage, "auction_project/kyc_documents", extraOptions);
            backUrl = cloudinaryService.uploadFile(backImage, "auction_project/kyc_documents", extraOptions);
        } catch (FileStorageException e) {
            log.error("Error uploading KYC images to Cloudinary", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
 
        // 4. Lưu thông tin và cập nhật trạng thái User thành VERIFIED
        user.verifyKyc(identity.dateOfBirth(), identity.idCard(), frontUrl, backUrl, identity.gender());
 
        userRepository.save(user);
        log.info("eKYC verified successfully for user profile: {}", userProfileId);
    }
}
