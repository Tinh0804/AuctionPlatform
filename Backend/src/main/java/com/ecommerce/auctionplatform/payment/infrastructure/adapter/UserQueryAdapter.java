package com.ecommerce.auctionplatform.payment.infrastructure.adapter;

import com.ecommerce.auctionplatform.payment.application.port.out.UserPort;
import com.ecommerce.auctionplatform.payment.application.port.out.UserView;
import com.ecommerce.auctionplatform.identity.application.port.in.ReputationUseCase;
import com.ecommerce.auctionplatform.identity.application.port.in.UserDirectoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserPort {
    private final UserDirectoryUseCase userDirectoryUseCase;
    private final ReputationUseCase reputationUseCase;

    @Override
    public Optional<UserView> findById(UUID id) {
        return userDirectoryUseCase.findById(id).map(this::toView);
    }

    @Override
    public Optional<UserView> findAdminUser() {
        return userDirectoryUseCase.findAdmin().map(this::toView);
    }

    @Override
    public void changeReputation(UUID userId, int scoreChange, String reason, UUID orderId) {
        reputationUseCase.changeForOrder(userId, scoreChange, reason, orderId);
    }

    private UserView toView(UserDirectoryUseCase.UserProfile user) {
        return new UserView(user.id(), user.name(), user.phone(), user.reputationScore());
    }
}
