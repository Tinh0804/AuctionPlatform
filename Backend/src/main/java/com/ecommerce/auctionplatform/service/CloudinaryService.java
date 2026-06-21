package com.ecommerce.auctionplatform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        return uploadFile(file, folderName, null);
    }

    public String uploadFile(MultipartFile file, String folderName, Map<String, Object> extraOptions) throws IOException {
        try {
            String targetFolder = folderName.startsWith("auction_") ? folderName : "auction_platform/" + folderName;
            java.util.Map<String, Object> options = new java.util.HashMap<>();
            options.put("folder", targetFolder);
            options.put("public_id", java.util.UUID.randomUUID().toString());
            if (extraOptions != null) {
                options.putAll(extraOptions);
            }
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("Error uploading file to Cloudinary", e);
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }
}
