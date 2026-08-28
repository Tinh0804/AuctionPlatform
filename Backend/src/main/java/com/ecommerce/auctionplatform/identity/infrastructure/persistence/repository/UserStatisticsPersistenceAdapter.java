package com.ecommerce.auctionplatform.identity.infrastructure.persistence.repository;

import com.ecommerce.auctionplatform.identity.application.port.out.UserStatisticsQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserStatisticsPersistenceAdapter implements UserStatisticsQueryPort {
    private final UserJpaRepository userJpaRepository;

    @Override
    public long countUsers() {
        return userJpaRepository.count();
    }
}
