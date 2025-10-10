package com.nakacorp.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que aplica rate limiting em requisições HTTP.
 *
 * Verifica se o cliente (identificado por IP) excedeu o limite de requisições
 * e retorna HTTP 429 (Too Many Requests) quando o limite é atingido.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private final RateLimitConfig rateLimitConfig;

    public RateLimitInterceptor(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = getClientIP(request);
        RateLimitConfig.RateLimiterInfo limiterInfo = rateLimitConfig.resolveRateLimiter(ip);

        // Tenta adquirir permissão com timeout de 0 (não bloqueia)
        boolean acquired = limiterInfo.getRateLimiter().tryAcquire();

        if (acquired) {
            limiterInfo.incrementRequestCount();
            int requestCount = limiterInfo.getRequestCount();
            int remaining = Math.max(0, MAX_REQUESTS_PER_MINUTE - requestCount);

            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));

            return true;
        }

        // Rate limit excedido
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", "60");
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\": \"Too many requests\", " +
            "\"message\": \"Rate limit exceeded. Maximum " + MAX_REQUESTS_PER_MINUTE + " requests per minute. Try again in 60 seconds.\", " +
            "\"retryAfter\": 60}"
        );

        logger.warn("Rate limit exceeded for IP: {} - Request count: {}", ip, limiterInfo.getRequestCount());

        return false;
    }

    /**
     * Obtém o IP real do cliente, considerando proxies e load balancers.
     *
     * @param request requisição HTTP
     * @return endereço IP do cliente
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}