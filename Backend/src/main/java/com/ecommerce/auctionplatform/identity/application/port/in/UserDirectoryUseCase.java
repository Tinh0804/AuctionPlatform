package com.ecommerce.auctionplatform.identity.application.port.in;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Public, read-only view of user data for other bounded contexts. */
public interface UserDirectoryUseCase {
    Optional<UserProfile> findById(UUID userId);

    Map<UUID, UserProfile> findByIds(Iterable<UUID> userIds);

    Optional<UserProfile> findAdmin();

    record UserProfile(
            UUID id,
            String name,
            String phone,
            LocalDate dateOfBirth,
            boolean verified,
            int reputationScore
    ) {
    }
}
