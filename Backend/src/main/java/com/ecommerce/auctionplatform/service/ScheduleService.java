package com.ecommerce.auctionplatform.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleService {
    
    RedisTemplate<String, Object> redisTemplate;
    
    static final String PENDING_ACTIVATIONS_KEY = "auction:pending_activations";
    static final String PENDING_CLOSURES_KEY = "auction:pending_closures";

    public void scheduleAuctionActivation(String auctionId, LocalDateTime startTime) {
        long score = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        redisTemplate.opsForZSet().add(PENDING_ACTIVATIONS_KEY, auctionId, score);
        log.info("Scheduled auction {} to activate at {}", auctionId, startTime);
    }

    public void scheduleAuctionClosure(String auctionId, LocalDateTime endTime) {
        long score = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        redisTemplate.opsForZSet().add(PENDING_CLOSURES_KEY, auctionId, score);
        log.info("Scheduled auction {} to close at {}", auctionId, endTime);
    }
    
    public void removeAuctionActivation(String auctionId) {
        redisTemplate.opsForZSet().remove(PENDING_ACTIVATIONS_KEY, auctionId);
    }
    
    public void removeAuctionClosure(String auctionId) {
        redisTemplate.opsForZSet().remove(PENDING_CLOSURES_KEY, auctionId);
    }
}
