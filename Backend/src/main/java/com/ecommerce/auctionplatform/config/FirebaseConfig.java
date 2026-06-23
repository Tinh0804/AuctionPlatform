package com.ecommerce.auctionplatform.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${app.firebase.config-path}")
    private String configPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options;
                Resource resource = resourceLoader.getResource(configPath);
                
                if (resource.exists()) {
                    try (InputStream serviceAccount = resource.getInputStream()) {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();
                        FirebaseApp.initializeApp(options);
                        log.info("Firebase SDK initialized successfully using config file from: {}", configPath);
                    }
                } else {
                    log.warn("Firebase config file not found at: {}. Attempting fallback to Application Default Credentials.", configPath);
                    try {
                        options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.getApplicationDefault())
                                .build();
                        FirebaseApp.initializeApp(options);
                        log.info("Firebase SDK initialized successfully using Application Default Credentials.");
                    } catch (IOException e) {
                        log.error("Could not initialize Firebase: No config file found and Application Default Credentials are unavailable.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unexpected error initializing Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }
}
