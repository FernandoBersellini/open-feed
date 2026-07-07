package com.senhorcafe.openfeed.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenDenylist {

    private final Map<String, Instant> revokedTokenIds = new ConcurrentHashMap<>();

    public void revoke(String tokenId, Instant expiresAt) {
        revokedTokenIds.put(tokenId, expiresAt);
    }

    public boolean isRevoked(String tokenId) {
        return revokedTokenIds.containsKey(tokenId);
    }

    @Scheduled(fixedRate = 15, initialDelay = 15, timeUnit = java.util.concurrent.TimeUnit.MINUTES)
    void removeExpiredEntries() {
        Instant now = Instant.now();
        revokedTokenIds.values().removeIf(expiresAt -> expiresAt.isBefore(now));
    }
}
