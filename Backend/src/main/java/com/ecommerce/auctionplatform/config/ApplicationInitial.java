package com.ecommerce.auctionplatform.config;

import com.ecommerce.auctionplatform.entity.Account;
import com.ecommerce.auctionplatform.entity.Role;
import com.ecommerce.auctionplatform.entity.User;
import com.ecommerce.auctionplatform.entity.enums.PredefinedRole;
import com.ecommerce.auctionplatform.entity.enums.ProviderType;
import com.ecommerce.auctionplatform.entity.enums.VerificationStatus;
import com.ecommerce.auctionplatform.exception.AppException;
import com.ecommerce.auctionplatform.exception.ErrorCode;
import com.ecommerce.auctionplatform.repository.AccountRepository;
import com.ecommerce.auctionplatform.repository.RoleRepository;
import com.ecommerce.auctionplatform.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApplicationInitial {
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    AccountRepository accountRepository;
    UserRepository userRepository;

    static final String USERNAME_ADMIN = "admin";
    static final String PASSWORD_ADMIN = "admin";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Transactional   // thêm transaction để đảm bảo toàn vẹn dữ liệu
    ApplicationRunner applicationRunner() {
        return args -> {
            // 1. Tạo role ADMIN nếu chưa có
            if (!roleRepository.existsByName(PredefinedRole.RoleName.ADMIN)) {
                Role adminRoleEntity = Role.builder()// nếu id là String
                        .name(PredefinedRole.RoleName.ADMIN)
                        .description("Quản trị viên")
                        .build();
                roleRepository.save(adminRoleEntity);
                log.info("Đã tạo Role ADMIN");
            }

            // Lấy role Admin (đã tồn tại)
            Role adminRole = roleRepository.findByName(PredefinedRole.RoleName.ADMIN.toUpperCase())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            // 2. Tạo tài khoản admin nếu chưa tồn tại
            if (!accountRepository.existsByUsername(USERNAME_ADMIN)) {
                Account account = Account.builder()
                        .username(USERNAME_ADMIN)
                        .password(passwordEncoder.encode(PASSWORD_ADMIN))
                        .role(adminRole)
                        .isActive(true)
                        .provider(ProviderType.LOCAL)
                        .build();

                User adminProfile = User.builder()
                        .name("System Admin")
                        .phone("0366900821")
                        .email("lhqtinh2005@gmail.com")
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .gender(true)
                        .account(account)
                        .build();
                userRepository.save(adminProfile);

                log.warn(">>> Account Admin khởi tạo thành công với mật khẩu: {}", PASSWORD_ADMIN);
            } else {
                log.info("Tài khoản Admin đã tồn tại.");
            }
        };
    }
}