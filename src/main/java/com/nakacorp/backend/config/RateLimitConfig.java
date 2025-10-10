package com.nakacorp.backend.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuração de Rate Limiting para proteção contra DDoS e abuso de APIs.
 *
 * Implementa limitação de requisições por IP usando Guava RateLimiter.
 * Protege endpoints críticos como login, registro e APIs públicas.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /**
     * Cache de rate limiters por endereço IP.
     * Cada IP tem seu próprio rate limiter para controle de taxa.
     */
    private final Map<String, RateLimiterInfo> cache = new ConcurrentHashMap<>();

    /**
     * Obtém ou cria um rate limiter para um IP específico.
     *
     * Limites configurados:
     * - 100 requisições por minuto por IP (1.67 req/segundo)
     *
     * @param key endereço IP do cliente
     * @return rate limiter para o IP
     */
    public RateLimiterInfo resolveRateLimiter(String key) {
        return cache.computeIfAbsent(key, k -> createNewRateLimiter());
    }

    /**
     * Cria um novo rate limiter com as configurações.
     *
     * @return novo rate limiter configurado
     */
    private RateLimiterInfo createNewRateLimiter() {
        // 100 requisições por minuto = 1.67 requisições por segundo
        RateLimiter rateLimiter = RateLimiter.create(1.67);
        return new RateLimiterInfo(rateLimiter, 0);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(this))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health/**", "/api/swagger-ui/**", "/api/v3/api-docs/**");
    }

    /**
     * Remove rate limiters não utilizados do cache para liberar memória.
     * Deve ser chamado periodicamente por um scheduler.
     */
    public void cleanupExpiredLimiters() {
        long currentTime = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> {
            long timeSinceLastUse = currentTime - entry.getValue().getLastAccessTime();
            return timeSinceLastUse > 3600000; // Remove após 1 hora sem uso
        });
    }

    /**
     * Classe interna para armazenar rate limiter e metadados.
     */
    public static class RateLimiterInfo {
        private final RateLimiter rateLimiter;
        private long lastAccessTime;
        private int requestCount;

        public RateLimiterInfo(RateLimiter rateLimiter, int requestCount) {
            this.rateLimiter = rateLimiter;
            this.requestCount = requestCount;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public RateLimiter getRateLimiter() {
            this.lastAccessTime = System.currentTimeMillis();
            return rateLimiter;
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }

        public void incrementRequestCount() {
            this.requestCount++;
            this.lastAccessTime = System.currentTimeMillis();
        }

        public int getRequestCount() {
            return requestCount;
        }

        public void resetRequestCount() {
            this.requestCount = 0;
        }
    }
}
