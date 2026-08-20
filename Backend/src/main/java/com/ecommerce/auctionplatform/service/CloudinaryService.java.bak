package com.ecommerce.auctionplatform.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {

    Cloudinary cloudinary;

    @NonFinal
    @Value("${spring.application.name}")
    protected String applicationName;

    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        return uploadFile(file, folderName, null);
    }

    public String uploadFile(MultipartFile file, String folderName, Map<String, Object> extraOptions) throws IOException {
        try {
            String targetFolder = folderName.startsWith(applicationName) ? folderName : applicationName + "/" + folderName;
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
