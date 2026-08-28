package com.ecommerce.auctionplatform.auction.infrastructure.adapter;

import com.ecommerce.auctionplatform.auction.application.port.out.UserStatsPort;
import com.ecommerce.auctionplatform.identity.application.port.in.UserStatisticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionUserStatsAdapter implements UserStatsPort {
    private final UserStatisticsUseCase userStatisticsUseCase;

    @Override
    public long countUsers() {
        return userStatisticsUseCase.countUsers();
    }
}
