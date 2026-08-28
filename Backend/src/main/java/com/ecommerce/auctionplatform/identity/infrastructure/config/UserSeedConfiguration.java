package com.ecommerce.auctionplatform.identity.infrastructure.config;

import com.ecommerce.auctionplatform.identity.application.port.in.UserBootstrapUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class UserSeedConfiguration {
    private final UserBootstrapUseCase userBootstrapUseCase;

    @Bean
    ApplicationRunner userSeedRunner(
            @Value("${app.seed.admin.username}") String adminUsername,
            @Value("${app.seed.admin.password}") String adminPassword,
            @Value("${app.seed.admin.phone}") String adminPhone,
            @Value("${app.seed.admin.email}") String adminEmail
    ) {
        return args -> {
            userBootstrapUseCase.ensureAdmin(new UserBootstrapUseCase.AdminBootstrapCommand(
                    adminUsername, adminPassword, adminPhone, adminEmail));
            log.warn("Đã kiểm tra cấu hình bootstrap admin; hãy xoay vòng mật khẩu sau lần đăng nhập đầu tiên");
        };
    }
}
