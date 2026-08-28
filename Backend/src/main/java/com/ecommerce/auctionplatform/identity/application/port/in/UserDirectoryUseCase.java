package com.ecommerce.auctionplatform.identity.application.port.in;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

/** Public, read-only view of user data for other bounded contexts. */
public interface UserDirectoryUseCase {
    Optional<UserProfile> findById(UUID userId);

    Map<UUID, UserProfile> findByIds(Iterable<UUID> userIds);

    Optional<UserProfile> findAdmin();

    List<UserProfile> findAll();

    List<UserProfile> findByRoleName(String roleName);

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
