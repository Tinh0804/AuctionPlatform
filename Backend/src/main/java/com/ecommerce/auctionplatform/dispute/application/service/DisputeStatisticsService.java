package com.ecommerce.auctionplatform.dispute.application.service;

import com.ecommerce.auctionplatform.dispute.application.port.in.DisputeStatisticsUseCase;
import com.ecommerce.auctionplatform.dispute.domain.repository.DisputeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisputeStatisticsService implements DisputeStatisticsUseCase {
    private final DisputeRepository disputeRepository;

    @Override
    @Transactional(readOnly = true)
    public long countDisputes() {
        return disputeRepository.count();
    }
}
