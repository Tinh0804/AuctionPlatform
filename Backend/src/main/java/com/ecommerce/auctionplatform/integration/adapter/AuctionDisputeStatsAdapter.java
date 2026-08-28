package com.ecommerce.auctionplatform.integration.adapter;

import com.ecommerce.auctionplatform.auction.application.port.out.DisputeStatsPort;
import com.ecommerce.auctionplatform.dispute.application.port.in.DisputeStatisticsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionDisputeStatsAdapter implements DisputeStatsPort {
    private final DisputeStatisticsUseCase disputeStatisticsUseCase;

    @Override
    public long countDisputes() {
        return disputeStatisticsUseCase.countDisputes();
    }
}
