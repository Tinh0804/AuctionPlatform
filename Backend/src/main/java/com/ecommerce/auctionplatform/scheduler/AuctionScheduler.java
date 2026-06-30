package com.ecommerce.auctionplatform.scheduler;

import com.ecommerce.auctionplatform.service.AuctionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuctionScheduler {

    RedisTemplate<String, Object> redisTemplate;
    AuctionService auctionService;

    static final String PENDING_ACTIVATIONS_KEY = "auction:pending_activations";
    static final String PENDING_CLOSURES_KEY = "auction:pending_closures";

    @Scheduled(fixedRate = 2000) 
    public void processActivations() {
        long currentScore = Instant.now().toEpochMilli();
        Set<ZSetOperations.TypedTuple<Object>> pending = redisTemplate.opsForZSet().rangeByScoreWithScores(PENDING_ACTIVATIONS_KEY, 0, currentScore);

        if (pending != null && !pending.isEmpty()) {
            for (ZSetOperations.TypedTuple<Object> tuple : pending) {
                String auctionId = (String) tuple.getValue();
                try {
                    auctionService.activateAuction(auctionId);
                    redisTemplate.opsForZSet().remove(PENDING_ACTIVATIONS_KEY, auctionId);
                    log.info("Successfully activated auction {}", auctionId);
                } catch (Exception e) {
                    log.error("Failed to activate auction {}", auctionId, e);
                }
            }
        }
    }

    @Scheduled(fixedRate = 2000) 
    public void processClosures() {
        long currentScore = Instant.now().toEpochMilli();
        Set<ZSetOperations.TypedTuple<Object>> pending = redisTemplate.opsForZSet().rangeByScoreWithScores(PENDING_CLOSURES_KEY, 0, currentScore);

        if (pending != null && !pending.isEmpty()) {
            for (ZSetOperations.TypedTuple<Object> tuple : pending) {
                String auctionId = (String) tuple.getValue();
                try {
                    auctionService.closeAuction(auctionId);
                    redisTemplate.opsForZSet().remove(PENDING_CLOSURES_KEY, auctionId);
                    log.info("Successfully closed auction {}", auctionId);
                } catch (Exception e) {
                    log.error("Failed to close auction {}", auctionId, e);
                }
            }
        }
    }
}
