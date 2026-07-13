package com.ecommerce.auctionplatform.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class WebSocketEventListener {

    SimpMessagingTemplate messagingTemplate;
    StringRedisTemplate redisTemplate;

    // Map of sessionId -> Set of subscriptionIds
    Map<String, Set<String>> sessionSubscriptions = new ConcurrentHashMap<>();
    
    // Map of subscriptionId -> auctionId
    Map<String, String> subscriptionToAuction = new ConcurrentHashMap<>();

    static final String VIEWER_COUNT_KEY_PREFIX = "auction:viewers:";

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();

        if (destination != null && destination.matches("^/topic/auction/[a-zA-Z0-9\\-]+$"))  { //bỏ qua kênh /topic/auction
            String auctionId = extractAuctionId(destination);
            if (auctionId != null && subscriptionId != null) {
                subscriptionToAuction.put(subscriptionId, auctionId);
                sessionSubscriptions.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(subscriptionId);
                
                Long viewers = redisTemplate.opsForValue().increment(VIEWER_COUNT_KEY_PREFIX + auctionId);
                broadcastViewerCount(auctionId, viewers);
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String subscriptionId = headerAccessor.getSubscriptionId();
        String sessionId = headerAccessor.getSessionId();

        if (subscriptionId != null) {
            String auctionId = subscriptionToAuction.remove(subscriptionId);
            if (auctionId != null) {
                java.util.Set<String> subs = sessionSubscriptions.get(sessionId);
                if (subs != null) {
                    subs.remove(subscriptionId);
                    if (subs.isEmpty()) {
                        sessionSubscriptions.remove(sessionId);
                    }
                }
                decrementAndBroadcast(auctionId);
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        java.util.Set<String> subs = sessionSubscriptions.remove(sessionId);
        if (subs != null) {
            for (String subId : subs) {
                String auctionId = subscriptionToAuction.remove(subId);
                if (auctionId != null) {
                    decrementAndBroadcast(auctionId);
                }
            }
        }
    }

    private void decrementAndBroadcast(String auctionId) {
        Long viewers = redisTemplate.opsForValue().decrement(VIEWER_COUNT_KEY_PREFIX + auctionId);
        if (viewers != null && viewers < 0) {
            redisTemplate.opsForValue().set(VIEWER_COUNT_KEY_PREFIX + auctionId, "0");
            viewers = 0L;
        }
        broadcastViewerCount(auctionId, viewers);
    }

    private String extractAuctionId(String destination) {
        try {
            String[] parts = destination.split("/");
            if (parts.length >= 4) {
                // e.g. /topic/auction/123-abc
                return parts[3];
            }
        } catch (Exception e) {
            log.error("Error extracting auction ID from destination: " + destination, e);
        }
        return null;
    }

    private void broadcastViewerCount(String auctionId, Long viewers) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "viewer_count");
        msg.put("viewer_count", viewers != null ? viewers : 0);
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId + "/status", (Object) msg);
    }
}
