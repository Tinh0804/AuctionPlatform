package com.ecommerce.auctionplatform.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;


@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class BlackListService {
    RedisTemplate<String, Object> redisTemplate;

    String BLACKLIST_PREFIX = "BLACKLIST";

    public void addToBlackList(String token, long remainingTime) {
        String key = BLACKLIST_PREFIX + ":" + token;
        redisTemplate.opsForValue().set(key,"revoked",remainingTime, TimeUnit.MILLISECONDS);
    }


    public boolean isBlackListed(String token) {
        String key = BLACKLIST_PREFIX + ":" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
