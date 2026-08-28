package com.ecommerce.auctionplatform.auction.infrastructure.adapter;

import com.ecommerce.auctionplatform.auction.application.port.out.AuctionUserPort;
import com.ecommerce.auctionplatform.auction.application.port.out.AuctionUserView;
import com.ecommerce.auctionplatform.identity.application.port.in.UserDirectoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuctionUserAdapter implements AuctionUserPort {
    private final UserDirectoryUseCase userDirectoryUseCase;

    @Override
    public Optional<AuctionUserView> findById(UUID userId) {
        return userDirectoryUseCase.findById(userId).map(user -> new AuctionUserView(
                user.id(), user.name(), user.dateOfBirth(), user.verified(), user.reputationScore()));
    }

    @Override
    public java.util.Map<UUID, AuctionUserView> findByIds(Iterable<UUID> userIds) {
        java.util.Map<UUID, UserDirectoryUseCase.UserProfile> profiles = userDirectoryUseCase.findByIds(userIds);
        java.util.Map<UUID, AuctionUserView> result = new java.util.HashMap<>();
        profiles.forEach((id, user) -> result.put(id, new AuctionUserView(
                user.id(), user.name(), user.dateOfBirth(), user.verified(), user.reputationScore())));
        return result;
    }
}
