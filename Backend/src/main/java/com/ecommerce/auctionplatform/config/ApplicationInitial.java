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
@Configuration
public class ApplicationInitial {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    static final String USERNAME_ADMIN = "admin";
    static final String PASSWORD_ADMIN = "admin";


    @Bean
    @Transactional
    ApplicationRunner applicationRunner() {
        return args -> {
            if (!roleRepository.existsByName(PredefinedRole.RoleName.ADMIN)) {
                Role adminRoleEntity = Role.builder()
                        .name(PredefinedRole.RoleName.ADMIN)
                        .description("Quản trị viên")
                        .build();
                roleRepository.save(adminRoleEntity);
                log.info("Đã tạo Role ADMIN");
            }
            if(!roleRepository.existsByName(PredefinedRole.RoleName.USER)){
                Role userRoleEntity = Role.builder()
                        .name(PredefinedRole.RoleName.USER)
                        .description("Người dùng")
                        .build();
                roleRepository.save(userRoleEntity);
                log.info("Đã tạo Role USER");
            }

            Role adminRole = roleRepository.findByName(PredefinedRole.RoleName.ADMIN.toUpperCase())
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));


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