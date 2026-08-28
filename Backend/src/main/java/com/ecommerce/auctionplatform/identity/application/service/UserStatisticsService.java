package com.ecommerce.auctionplatform.identity.application.service;

import com.ecommerce.auctionplatform.identity.application.port.in.UserStatisticsUseCase;
import com.ecommerce.auctionplatform.identity.application.port.out.UserStatisticsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatisticsService implements UserStatisticsUseCase {
    private final UserStatisticsQueryPort statisticsQueryPort;

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return statisticsQueryPort.countUsers();
    }
}
