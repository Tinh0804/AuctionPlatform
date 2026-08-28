package com.ecommerce.auctionplatform.auction.infrastructure.scheduler;

import com.ecommerce.auctionplatform.auction.application.port.in.AuctionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisKeyExpirationListener implements MessageListener {

    private final AuctionUseCase auctionUseCase;

    private static final String ACTIVATE_KEY_PREFIX = RedisAuctionScheduleAdapter.ACTIVATE_KEY_PREFIX;
    private static final String CLOSE_KEY_PREFIX = RedisAuctionScheduleAdapter.CLOSE_KEY_PREFIX;
    private static final String PAYMENT_EXPIRY_PREFIX = RedisAuctionScheduleAdapter.PAYMENT_EXPIRY_PREFIX;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        try {
            if (expiredKey.startsWith(ACTIVATE_KEY_PREFIX)) {
                String auctionId = expiredKey.substring(ACTIVATE_KEY_PREFIX.length());
                log.info("Redis TTL expired → Activating auction {}", auctionId);
                auctionUseCase.activateAuction(auctionId);

            } else if (expiredKey.startsWith(CLOSE_KEY_PREFIX)) {
                String auctionId = expiredKey.substring(CLOSE_KEY_PREFIX.length());
                log.info("Redis TTL expired → Closing auction {}", auctionId);
                auctionUseCase.closeAuction(auctionId);
                
            } else if (expiredKey.startsWith(PAYMENT_EXPIRY_PREFIX)) {
                String recordIdStr = expiredKey.substring(PAYMENT_EXPIRY_PREFIX.length());
                log.info("Redis TTL expired → Handling abandoned payment for record {}", recordIdStr);
                try {
                    UUID recordId = UUID.fromString(recordIdStr);
                    auctionUseCase.handlePaymentExpiry(recordId);
                } catch (Exception ex) {
                    log.error("Could not process payment expiry for record {}", recordIdStr, ex);
                }
            }
        } catch (Exception e) {
            log.error("Error handling expired key {}: {}", expiredKey, e.getMessage(), e);
        }
    }
}
