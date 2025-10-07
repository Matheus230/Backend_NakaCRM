package com.nakacorp.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuração de cache usando Caffeine.
 * <p>
 * Configura diferentes políticas de cache para diferentes tipos de dados:
 * - produtos: cache de 1 hora
 * - clientes: cache de 15 minutos
 * - dashboard-stats: cache de 5 minutos
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configuração do gerenciador de cache Caffeine.
     *
     * @return CacheManager configurado
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
            "produtos",
            "clientes",
            "dashboard-stats",
            "usuarios"
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .recordStats());

        return cacheManager;
    }

    /**
     * Cache específico para produtos com TTL de 1 hora.
     */
    @Bean
    public Caffeine<Object, Object> produtosCaffeineConfig() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .recordStats();
    }

    /**
     * Cache específico para stats do dashboard com TTL de 5 minutos.
     */
    @Bean
    public Caffeine<Object, Object> dashboardCaffeineConfig() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats();
    }
}
