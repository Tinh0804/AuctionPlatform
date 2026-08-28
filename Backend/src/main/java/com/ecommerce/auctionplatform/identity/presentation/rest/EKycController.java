package com.ecommerce.auctionplatform.identity.presentation.rest;
 
import com.ecommerce.auctionplatform.shared.presentation.response.APIResponse;
import com.ecommerce.auctionplatform.shared.presentation.mapper.FileUploadMapper;
import com.ecommerce.auctionplatform.identity.application.port.in.EKycUseCase;
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
 
    EKycUseCase eKycUseCase;
 
    @PostMapping("/verify")
    public APIResponse<Void> verifyKyc(
            @RequestParam("front_image") MultipartFile frontImage,
            @RequestParam("back_image") MultipartFile backImage) {
 
        eKycUseCase.verifyKyc(
                FileUploadMapper.toContent(frontImage),
                FileUploadMapper.toContent(backImage));
 
        return APIResponse.<Void>builder()
                .status(200)
                .message("eKYC verification completed successfully")
                .build();
    }
}
