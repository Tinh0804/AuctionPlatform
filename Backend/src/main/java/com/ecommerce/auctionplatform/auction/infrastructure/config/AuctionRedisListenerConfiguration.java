package com.ecommerce.auctionplatform.auction.infrastructure.config;

import com.ecommerce.auctionplatform.auction.infrastructure.scheduler.RedisKeyExpirationListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class AuctionRedisListenerConfiguration {
    @Bean
    @ConditionalOnProperty(name = "app.redis.listener-enabled", havingValue = "true", matchIfMissing = true)
    RedisMessageListenerContainer auctionRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener expirationListener
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(expirationListener, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }
}
