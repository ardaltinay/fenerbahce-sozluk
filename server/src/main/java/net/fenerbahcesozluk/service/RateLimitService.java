package net.fenerbahcesozluk.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory rate limiting service. Limits requests per IP for specific
 * endpoints.
 */
@Service
public class RateLimitService {

    // Map to store request counts: key = "endpoint:ip", value = RequestInfo
    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    // Rate limit configurations
    private static final Map<String, RateLimitConfig> RATE_LIMITS = Map.of(
            "login", new RateLimitConfig(5, 1), // 5 per minute
            "register", new RateLimitConfig(3, 60), // 3 per hour
            "forgot-password", new RateLimitConfig(3, 60), // 3 per hour
            "contact", new RateLimitConfig(3, 60), // 3 per hour
            "entry-create", new RateLimitConfig(10, 1), // 10 per minute
            "topic-create", new RateLimitConfig(5, 5), // 5 per 5 minutes
            "vote", new RateLimitConfig(30, 1) // 30 per minute
    );

    /**
     * Check if the request should be rate limited.
     * 
     * @param endpoint The endpoint type
     * @param ip       The client IP address
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isAllowed(String endpoint, String ip) {
        RateLimitConfig config = RATE_LIMITS.get(endpoint);
        if (config == null) {
            return true; // No rate limiting for unconfigured endpoints
        }

        String key = endpoint + ":" + ip;
        long now = System.currentTimeMillis();
        long windowMs = TimeUnit.MINUTES.toMillis(config.windowMinutes);

        RequestInfo info = requestCounts.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                // Start new window
                return new RequestInfo(now, 1);
            } else {
                // Increment count in existing window
                existing.count++;
                return existing;
            }
        });

        return info.count <= config.maxRequests;
    }

    /**
     * Get remaining requests for an endpoint/IP combination.
     */
    public int getRemainingRequests(String endpoint, String ip) {
        RateLimitConfig config = RATE_LIMITS.get(endpoint);
        if (config == null) {
            return Integer.MAX_VALUE;
        }

        String key = endpoint + ":" + ip;
        long now = System.currentTimeMillis();
        long windowMs = TimeUnit.MINUTES.toMillis(config.windowMinutes);

        RequestInfo info = requestCounts.get(key);
        if (info == null || now - info.windowStart > windowMs) {
            return config.maxRequests;
        }

        return Math.max(0, config.maxRequests - info.count);
    }

    /**
     * Get seconds until rate limit resets.
     */
    public long getSecondsUntilReset(String endpoint, String ip) {
        RateLimitConfig config = RATE_LIMITS.get(endpoint);
        if (config == null) {
            return 0;
        }

        String key = endpoint + ":" + ip;
        long now = System.currentTimeMillis();
        long windowMs = TimeUnit.MINUTES.toMillis(config.windowMinutes);

        RequestInfo info = requestCounts.get(key);
        if (info == null) {
            return 0;
        }

        long resetTime = info.windowStart + windowMs;
        return Math.max(0, (resetTime - now) / 1000);
    }

    /**
     * Periodically clean up expired entries (call this from a scheduled task if
     * needed)
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        long maxWindow = TimeUnit.MINUTES.toMillis(60); // Use longest window (1 hour)

        requestCounts.entrySet().removeIf(entry -> now - entry.getValue().windowStart > maxWindow);
    }

    private static class RateLimitConfig {
        final int maxRequests;
        final long windowMinutes;

        RateLimitConfig(int maxRequests, long windowMinutes) {
            this.maxRequests = maxRequests;
            this.windowMinutes = windowMinutes;
        }
    }

    private static class RequestInfo {
        long windowStart;
        int count;

        RequestInfo(long windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
