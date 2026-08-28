package com.ecommerce.auctionplatform.auction.infrastructure.scheduler;

import com.ecommerce.auctionplatform.auction.application.port.out.AuctionSchedulePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisAuctionScheduleAdapter implements AuctionSchedulePort {
    public static final String ACTIVATE_KEY_PREFIX = "auction:activate:";
    public static final String CLOSE_KEY_PREFIX = "auction:close:";
    public static final String PAYMENT_EXPIRY_PREFIX = "auction:payment:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void scheduleActivation(String auctionId, LocalDateTime startTime) {
        schedule(ACTIVATE_KEY_PREFIX + auctionId, startTime);
    }

    @Override
    public void scheduleClosure(String auctionId, LocalDateTime endTime) {
        schedule(CLOSE_KEY_PREFIX + auctionId, endTime);
    }

    @Override
    public void schedulePaymentExpiry(String recordId, LocalDateTime expiryTime) {
        schedule(PAYMENT_EXPIRY_PREFIX + recordId, expiryTime);
    }

    private void schedule(String key, LocalDateTime time) {
        long delaySeconds = Math.max(1, Duration.between(LocalDateTime.now(), time).getSeconds());
        redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(delaySeconds));
        log.debug("Scheduled Redis key {} with TTL={}s", key, delaySeconds);
    }
}
