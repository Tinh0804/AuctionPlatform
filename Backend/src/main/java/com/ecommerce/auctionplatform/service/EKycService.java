package com.ecommerce.auctionplatform.service;
 
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.UserRepository;
import com.ecommerce.auctionplatform.utils.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
 
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EKycService {
 
    final UserRepository userRepository;
    final CloudinaryService cloudinaryService;
    final ObjectMapper objectMapper;
 
    @Value("${app.fptai.key}")
    String fptAiKey;
 
    public void verifyKyc(MultipartFile frontImage, MultipartFile backImage) {
        UUID userProfileId = UUID.fromString(SecurityUtils.getCurrentProfileId().orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHORIZED)));
 
        User user = userRepository.findById(userProfileId).orElseThrow(() ->
                new AppException(ErrorCode.USER_NOT_FOUND));
 
        String idCard = "";
        String genderStr = "";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", fptAiKey);
 
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(frontImage.getBytes()) {
                @Override
                public String getFilename() {
                    return frontImage.getOriginalFilename();
                }
            };
            body.add("image", fileResource);
 
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.fpt.ai/vision/idr/vnm", requestEntity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("errorCode") && root.get("errorCode").asInt() == 0) {
                JsonNode dataNode = root.get("data");
                if (dataNode.isArray() && dataNode.size() > 0) {
                    JsonNode firstData = dataNode.get(0);
                    idCard = firstData.has("id") ? firstData.get("id").asText() : "";
                    genderStr = firstData.has("sex") ? firstData.get("sex").asText() : "";
                    String dobStr = firstData.has("dob") ? firstData.get("dob").asText() : "";
                    if (!dobStr.isEmpty()) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            user.setDob(LocalDate.parse(dobStr.replace("-", "/"), formatter));
                        } catch (Exception ex) {
                            log.warn("Failed to parse dob from eKYC: {}", dobStr);
                        }
                    }
                }
            } else {
                String errorMsg = root.has("errorMessage") ? root.get("errorMessage").asText() : "OCR failed";
                log.error("FPT AI OCR returned error: {}", errorMsg);
                throw new AppException(ErrorCode.INVALID_EKYC_IMAGE);
            }
        } catch (IOException e) {
            log.error("Error reading front image bytes", e);
            throw new AppException(ErrorCode.INVALID_EKYC_IMAGE);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling FPT AI OCR API", e);
            throw new AppException(ErrorCode.INVALID_EKYC_IMAGE);
        }
 
        if (idCard == null || idCard.trim().isEmpty()) {
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
        } catch (IOException e) {
            log.error("Error uploading KYC images to Cloudinary", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
 
        // 4. Lưu thông tin và cập nhật trạng thái User thành VERIFIED
        user.setIdentityCard(idCard);
        user.setIdentityFrontImage(frontUrl);
        user.setIdentityBackImage(backUrl);
        user.setGender("Nam".equalsIgnoreCase(genderStr) || "Male".equalsIgnoreCase(genderStr) || "true".equalsIgnoreCase(genderStr));
        user.setVerificationStatus(VerificationStatus.VERIFIED);
 
        userRepository.save(user);
        log.info("eKYC verified successfully for user profile: {}, ID Card: {}", userProfileId, idCard);
    }
}
