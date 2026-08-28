package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.identity.application.port.in.UserDirectoryUseCase;
import com.ecommerce.auctionplatform.identity.domain.enums.PredefinedRole;
import com.ecommerce.auctionplatform.identity.domain.enums.VerificationStatus;
import com.ecommerce.auctionplatform.identity.domain.model.User;
import com.ecommerce.auctionplatform.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDirectoryService implements UserDirectoryUseCase {
    private final UserRepository userRepository;

    @Override
    public Optional<UserProfile> findById(UUID userId) {
        return userRepository.findById(userId).map(this::toProfile);
    }

    @Override
    public java.util.Map<UUID, UserProfile> findByIds(Iterable<UUID> userIds) {
        if (userIds == null) return java.util.Map.of();
        return userRepository.findAllById(userIds).stream()
                .map(this::toProfile)
                .collect(java.util.stream.Collectors.toMap(UserProfile::id, p -> p, (a, b) -> a));
    }

    @Override
    public Optional<UserProfile> findAdmin() {
        return userRepository.findFirstByAccountRoleName(PredefinedRole.ADMIN.name()).map(this::toProfile);
    }

    private UserProfile toProfile(User user) {
        return new UserProfile(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getDob(),
                user.getVerificationStatus() == VerificationStatus.VERIFIED,
                user.getReputationScore() == null ? 0 : user.getReputationScore());
    }
}
