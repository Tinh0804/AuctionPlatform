package com.ecommerce.auctionplatform.config;

import com.ecommerce.auctionplatform.entity.AuctionRecord;
import com.ecommerce.auctionplatform.entity.enums.AuctionRecordStatus;
import com.ecommerce.auctionplatform.service.AuctionService;
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

    private final AuctionService auctionService;

    private static final String ACTIVATE_KEY_PREFIX = "auction:activate:";
    private static final String CLOSE_KEY_PREFIX = "auction:close:";
    private static final String PAYMENT_EXPIRY_PREFIX = "auction:payment:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        try {
            if (expiredKey.startsWith(ACTIVATE_KEY_PREFIX)) {
                String auctionId = expiredKey.substring(ACTIVATE_KEY_PREFIX.length());
                log.info("Redis TTL expired → Activating auction {}", auctionId);
                auctionService.activateAuction(auctionId);

            } else if (expiredKey.startsWith(CLOSE_KEY_PREFIX)) {
                String auctionId = expiredKey.substring(CLOSE_KEY_PREFIX.length());
                log.info("Redis TTL expired → Closing auction {}", auctionId);
                auctionService.closeAuction(auctionId);
                
            } else if (expiredKey.startsWith(PAYMENT_EXPIRY_PREFIX)) {
                String recordIdStr = expiredKey.substring(PAYMENT_EXPIRY_PREFIX.length());
                log.info("Redis TTL expired → Handling abandoned payment for record {}", recordIdStr);
                try {
                    UUID recordId = UUID.fromString(recordIdStr);
                    AuctionRecord record = auctionService.getAuctionRecord(recordId);
                    if (record.getStatus().equals(AuctionRecordStatus.PENDING_PAYMENT)) {
                        auctionService.handleOneAbandonedRecord(record);
                    }
                } catch (Exception ex) {
                    log.error("Could not process payment expiry for record {}", recordIdStr, ex);
                }
            }
        } catch (Exception e) {
            log.error("Error handling expired key {}: {}", expiredKey, e.getMessage(), e);
        }
    }
}
