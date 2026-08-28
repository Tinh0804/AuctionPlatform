package com.ecommerce.auctionplatform.shared.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface CurrentUserProvider {
    Optional<UUID> currentProfileId();
    Optional<UUID> currentAccountId();
    Optional<String> currentRole();
    Optional<String> currentToken();
}
