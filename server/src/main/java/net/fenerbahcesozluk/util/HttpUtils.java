package net.fenerbahcesozluk.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for HTTP request related operations.
 */
public final class HttpUtils {

  private HttpUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Extract client IP address from request, considering X-Forwarded-For header
   * for proxy/CDN/load balancer setups.
   *
   * @param request The HTTP request
   * @return The client's IP address
   */
  public static String getClientIp(HttpServletRequest request) {
    // Check X-Forwarded-For header (used by proxies/load balancers)
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      // Take the first IP in the chain (original client)
      return xForwardedFor.split(",")[0].trim();
    }

    // Check X-Real-IP header (used by some proxies like nginx)
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    // Fall back to remote address
    return request.getRemoteAddr();
  }
}
