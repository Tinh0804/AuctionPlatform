package com.ecommerce.auctionplatform.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleService {
    
    RedisTemplate<String, Object> redisTemplate;
    
    static final String ACTIVATE_KEY_PREFIX = "auction:activate:";
    static final String CLOSE_KEY_PREFIX = "auction:close:";
    static final String PAYMENT_EXPIRY_PREFIX = "auction:payment:";

    public void scheduleAuctionActivation(String auctionId, LocalDateTime startTime) {
        long delaySeconds = Duration.between(LocalDateTime.now(), startTime).getSeconds();
        if (delaySeconds <= 0) {
            // Thời gian cấu hình bắt đầu phiên đấu giá đã trôi qua, set TTL = 1 giây để kích hoạt ngay
            delaySeconds = 1;
        }
        redisTemplate.opsForValue().set(ACTIVATE_KEY_PREFIX + auctionId, "1", Duration.ofSeconds(delaySeconds));
        log.info("Scheduled auction {} to activate at {} (TTL={}s)", auctionId, startTime, delaySeconds);
    }

    public void scheduleAuctionClosure(String auctionId, LocalDateTime endTime) {
        long delaySeconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();
        if (delaySeconds <= 0) {
            delaySeconds = 1;
        }
        redisTemplate.opsForValue().set(CLOSE_KEY_PREFIX + auctionId, "1", Duration.ofSeconds(delaySeconds));
        log.info("Scheduled auction {} to close at {} (TTL={}s)", auctionId, endTime, delaySeconds);
    }
    
    public void removeAuctionActivation(String auctionId) {
        redisTemplate.delete(ACTIVATE_KEY_PREFIX + auctionId);
    }
    
    public void removeAuctionClosure(String auctionId) {
        redisTemplate.delete(CLOSE_KEY_PREFIX + auctionId);
    }
    
    public void schedulePaymentExpiry(String recordId, LocalDateTime expiryTime) {
        long delaySeconds = Duration.between(LocalDateTime.now(), expiryTime).getSeconds();
        if (delaySeconds <= 0) {
            delaySeconds = 1;
        }
        redisTemplate.opsForValue().set(PAYMENT_EXPIRY_PREFIX + recordId, "1", Duration.ofSeconds(delaySeconds));
        log.info("Scheduled payment expiry for record {} at {} (TTL={}s)", recordId, expiryTime, delaySeconds);
    }
}
