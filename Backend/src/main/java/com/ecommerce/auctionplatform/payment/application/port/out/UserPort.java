package com.ecommerce.auctionplatform.payment.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    Optional<UserView> findById(UUID id);

    Optional<UserView> findAdminUser();

    void changeReputation(UUID userId, int scoreChange, String reason, UUID orderId);
}
