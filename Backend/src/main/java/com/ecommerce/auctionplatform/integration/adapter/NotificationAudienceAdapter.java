package com.ecommerce.auctionplatform.integration.adapter;

import com.ecommerce.auctionplatform.identity.application.port.in.UserDirectoryUseCase;
import com.ecommerce.auctionplatform.notification.application.port.out.NotificationAudiencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotificationAudienceAdapter implements NotificationAudiencePort {
    private final UserDirectoryUseCase userDirectoryUseCase;

    @Override
    public Optional<AudienceUser> findById(UUID userId) {
        return userDirectoryUseCase.findById(userId).map(this::toUser);
    }

    @Override
    public List<AudienceUser> findAll() {
        return userDirectoryUseCase.findAll().stream().map(this::toUser).toList();
    }

    @Override
    public List<AudienceUser> findByRoleName(String roleName) {
        return userDirectoryUseCase.findByRoleName(roleName).stream().map(this::toUser).toList();
    }

    @Override
    public Map<UUID, AudienceUser> findByIds(Iterable<UUID> userIds) {
        return userDirectoryUseCase.findByIds(userIds).entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toUser(entry.getValue())));
    }

    private AudienceUser toUser(UserDirectoryUseCase.UserProfile profile) {
        return new AudienceUser(profile.id(), profile.name());
    }
}
