package com.ecommerce.auctionplatform.notification.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

public interface NotificationAudiencePort {
    Optional<AudienceUser> findById(UUID userId);

    List<AudienceUser> findAll();

    List<AudienceUser> findByRoleName(String roleName);

    Map<UUID, AudienceUser> findByIds(Iterable<UUID> userIds);

    record AudienceUser(UUID id, String name) {
    }
}
