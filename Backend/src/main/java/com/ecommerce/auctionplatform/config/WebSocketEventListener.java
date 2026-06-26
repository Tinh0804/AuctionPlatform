package com.ecommerce.auctionplatform.config;

import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    // Track session id -> destination map to handle disconnects properly since SessionDisconnectEvent might not have the destination
    private final Map<String, String> sessionDestinations = new ConcurrentHashMap<>();

    private static final String VIEWER_COUNT_KEY_PREFIX = "auction:viewers:";

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();

        if (destination != null && destination.startsWith("/topic/auction/")) {
            sessionDestinations.put(sessionId, destination);
            String auctionId = extractAuctionId(destination);
            if (auctionId != null) {
                Long viewers = redisTemplate.opsForValue().increment(VIEWER_COUNT_KEY_PREFIX + auctionId);
                broadcastViewerCount(auctionId, viewers);
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        handleDisconnection(sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        handleDisconnection(sessionId);
    }

    private void handleDisconnection(String sessionId) {
        String destination = sessionDestinations.remove(sessionId);
        if (destination != null && destination.startsWith("/topic/auction/")) {
            String auctionId = extractAuctionId(destination);
            if (auctionId != null) {
                Long viewers = redisTemplate.opsForValue().decrement(VIEWER_COUNT_KEY_PREFIX + auctionId);
                if (viewers != null && viewers < 0) {
                    redisTemplate.opsForValue().set(VIEWER_COUNT_KEY_PREFIX + auctionId, "0");
                    viewers = 0L;
                }
                broadcastViewerCount(auctionId, viewers);
            }
        }
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
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId + "/status", msg);
    }
}
