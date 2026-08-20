package com.ecommerce.auctionplatform.user.presentation.rest;
 
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.user.application.service.EKycService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
 
@RestController
@RequestMapping("/ekyc")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EKycController {
 
    EKycService eKycService;
 
    @PostMapping("/verify")
    public APIResponse<Void> verifyKyc(
            @RequestParam("front_image") MultipartFile frontImage,
            @RequestParam("back_image") MultipartFile backImage) {
 
        eKycService.verifyKyc(frontImage, backImage);
 
        return APIResponse.<Void>builder()
                .status(200)
                .message("eKYC verification completed successfully")
                .build();
    }
}
