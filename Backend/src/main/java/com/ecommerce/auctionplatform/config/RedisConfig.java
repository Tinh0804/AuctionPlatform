package com.ecommerce.auctionplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

        //Key is String
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //Value is JSON
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisKeyExpirationListener expirationListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Đảm bảo tính năng Keyspace Notifications được bật trên Redis Server
        try {
            connectionFactory.getConnection().setConfig("notify-keyspace-events", "Ex");
            log.info("Successfully enabled Redis keyspace notifications (notify-keyspace-events Ex)");
        } catch (Exception e) {
            log.warn("Could not configure Redis notify-keyspace-events dynamically. Please ensure it is enabled on the Redis server: {}", e.getMessage());
        }

        // Lắng nghe sự kiện key hết hạn trên tất cả database
        container.addMessageListener(expirationListener, new PatternTopic("__keyevent@*__:expired"));

        return container;
    }
}
