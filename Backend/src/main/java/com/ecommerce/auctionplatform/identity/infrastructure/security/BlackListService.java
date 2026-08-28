package com.ecommerce.auctionplatform.identity.infrastructure.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ecommerce.auctionplatform.identity.application.port.out.TokenBlacklist;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BlackListService implements TokenBlacklist {
    RedisTemplate<String, Object> redisTemplate;

    String BLACKLIST_PREFIX = "auction:BLACKLIST";

    public void addToBlackList(String token, long remainingTime) {
        String key = BLACKLIST_PREFIX + ":" + token;
        redisTemplate.opsForValue().set(key,"revoked",remainingTime, TimeUnit.MILLISECONDS);
    }


    public boolean isBlackListed(String token) {
        String key = BLACKLIST_PREFIX + ":" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void add(String token, long remainingTimeMillis) {
        addToBlackList(token, remainingTimeMillis);
    }

    @Override
    public boolean contains(String token) {
        return isBlackListed(token);
    }
}
