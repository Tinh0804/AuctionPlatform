package com.ecommerce.auctionplatform.dispute.infrastructure.adapter;

import com.ecommerce.auctionplatform.dispute.application.port.out.DisputePolicyPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfiguredDisputePolicy implements DisputePolicyPort {
    private final int daysToExpire;

    public ConfiguredDisputePolicy(@Value("${app.days-to-expire:7}") int daysToExpire) {
        this.daysToExpire = daysToExpire;
    }

    @Override
    public int daysToExpire() {
        return daysToExpire;
    }
}
