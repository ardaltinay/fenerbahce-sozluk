package net.fenerbahcesozluk.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory rate limiting service.
 * Limits requests per IP for specific endpoints.
 */
@Service
public class RateLimitService {

  // Map to store request counts: key = "endpoint:ip", value = RequestInfo
  private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

  // Rate limit configurations
  private static final int LOGIN_MAX_REQUESTS = 5;
  private static final long LOGIN_WINDOW_MINUTES = 1;

  private static final int REGISTER_MAX_REQUESTS = 3;
  private static final long REGISTER_WINDOW_MINUTES = 60;

  /**
   * Check if the request should be rate limited.
   * 
   * @param endpoint The endpoint type ("login" or "register")
   * @param ip       The client IP address
   * @return true if the request is allowed, false if rate limited
   */
  public boolean isAllowed(String endpoint, String ip) {
    String key = endpoint + ":" + ip;
    long now = System.currentTimeMillis();

    int maxRequests;
    long windowMs;

    switch (endpoint) {
      case "login":
        maxRequests = LOGIN_MAX_REQUESTS;
        windowMs = TimeUnit.MINUTES.toMillis(LOGIN_WINDOW_MINUTES);
        break;
      case "register":
        maxRequests = REGISTER_MAX_REQUESTS;
        windowMs = TimeUnit.MINUTES.toMillis(REGISTER_WINDOW_MINUTES);
        break;
      default:
        return true; // No rate limiting for unknown endpoints
    }

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

    return info.count <= maxRequests;
  }

  /**
   * Get remaining requests for an endpoint/IP combination.
   */
  public int getRemainingRequests(String endpoint, String ip) {
    String key = endpoint + ":" + ip;
    long now = System.currentTimeMillis();

    int maxRequests;
    long windowMs;

    switch (endpoint) {
      case "login":
        maxRequests = LOGIN_MAX_REQUESTS;
        windowMs = TimeUnit.MINUTES.toMillis(LOGIN_WINDOW_MINUTES);
        break;
      case "register":
        maxRequests = REGISTER_MAX_REQUESTS;
        windowMs = TimeUnit.MINUTES.toMillis(REGISTER_WINDOW_MINUTES);
        break;
      default:
        return Integer.MAX_VALUE;
    }

    RequestInfo info = requestCounts.get(key);
    if (info == null || now - info.windowStart > windowMs) {
      return maxRequests;
    }

    return Math.max(0, maxRequests - info.count);
  }

  /**
   * Get seconds until rate limit resets.
   */
  public long getSecondsUntilReset(String endpoint, String ip) {
    String key = endpoint + ":" + ip;
    long now = System.currentTimeMillis();

    long windowMs;

    switch (endpoint) {
      case "login":
        windowMs = TimeUnit.MINUTES.toMillis(LOGIN_WINDOW_MINUTES);
        break;
      case "register":
        windowMs = TimeUnit.MINUTES.toMillis(REGISTER_WINDOW_MINUTES);
        break;
      default:
        return 0;
    }

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
    long maxWindow = TimeUnit.MINUTES.toMillis(REGISTER_WINDOW_MINUTES); // Use longest window

    requestCounts.entrySet().removeIf(entry -> now - entry.getValue().windowStart > maxWindow);
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
