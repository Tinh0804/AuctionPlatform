package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.port.out.PasswordCodec;
import com.ecommerce.auctionplatform.identity.application.port.in.UserBootstrapUseCase;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.enums.ProviderType;
import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import com.ecommerce.auctionplatform.identity.domain.model.Account;
import com.ecommerce.auctionplatform.identity.domain.model.Role;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.AccountRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.RoleRepository;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBootstrapService implements UserBootstrapUseCase {
    private final PasswordCodec passwordCodec;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void ensureAdmin(AdminBootstrapCommand command) {
        validate(command);
        ensureRole(PredefinedRole.RoleName.ADMIN, "Quản trị viên");
        ensureRole(PredefinedRole.RoleName.USER, "Người dùng");
        if (accountRepository.existsByUsername(command.username())) {
            return;
        }
        Role adminRole = roleRepository.findByName(PredefinedRole.RoleName.ADMIN)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Account account = Account.builder()
                .username(command.username())
                .password(passwordCodec.encode(command.password()))
                .role(adminRole)
                .isActive(true)
                .provider(ProviderType.LOCAL)
                .build();
        userRepository.save(User.builder()
                .name("System Admin")
                .phone(command.phone())
                .email(command.email())
                .verificationStatus(VerificationStatus.VERIFIED)
                .gender(true)
                .account(account)
                .build());
    }

    private void ensureRole(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(Role.builder().name(name).description(description).build());
        }
    }

    private void validate(AdminBootstrapCommand command) {
        if (command == null || command.username() == null || command.username().isBlank()
                || command.password() == null || command.password().isBlank()
                || command.phone() == null || command.phone().isBlank()
                || command.email() == null || command.email().isBlank()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }
}
