package com.nakacorp.backend.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Serviço para controle de tentativas de login.
 *
 * Implementa proteção contra brute force attacks limitando o número de
 * tentativas de login por IP ou por usuário.
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 1.0
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = TimeUnit.MINUTES.toMillis(15);

    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    /**
     * Registra uma tentativa de login bem-sucedida.
     * Remove o registro de tentativas para a chave fornecida.
     *
     * @param key identificador (email ou IP)
     */
    public void loginSucceeded(String key) {
        loginAttempts.remove(key);
    }

    /**
     * Registra uma tentativa de login falhada.
     * Incrementa o contador de tentativas e registra o timestamp.
     *
     * @param key identificador (email ou IP)
     */
    public void loginFailed(String key) {
        LoginAttempt attempt = loginAttempts.getOrDefault(key, new LoginAttempt());
        attempt.incrementAttempts();
        attempt.setLastAttemptTime(System.currentTimeMillis());
        loginAttempts.put(key, attempt);
    }

    /**
     * Verifica se a chave está bloqueada por excesso de tentativas.
     *
     * @param key identificador (email ou IP)
     * @return true se bloqueado, false caso contrário
     */
    public boolean isBlocked(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null) {
            return false;
        }

        if (attempt.getAttempts() >= MAX_ATTEMPTS) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastAttempt = currentTime - attempt.getLastAttemptTime();

            if (timeSinceLastAttempt < LOCK_TIME_DURATION) {
                return true;
            } else {
                // Tempo de bloqueio expirou, remove o registro
                loginAttempts.remove(key);
                return false;
            }
        }

        return false;
    }

    /**
     * Retorna o tempo restante de bloqueio em segundos.
     *
     * @param key identificador (email ou IP)
     * @return segundos restantes de bloqueio, ou 0 se não estiver bloqueado
     */
    public long getRemainingBlockTime(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        if (attempt == null || attempt.getAttempts() < MAX_ATTEMPTS) {
            return 0;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastAttempt = currentTime - attempt.getLastAttemptTime();
        long remainingTime = LOCK_TIME_DURATION - timeSinceLastAttempt;

        return remainingTime > 0 ? TimeUnit.MILLISECONDS.toSeconds(remainingTime) : 0;
    }

    /**
     * Retorna o número de tentativas realizadas.
     *
     * @param key identificador (email ou IP)
     * @return número de tentativas
     */
    public int getAttempts(String key) {
        LoginAttempt attempt = loginAttempts.get(key);
        return attempt != null ? attempt.getAttempts() : 0;
    }

    /**
     * Limpa tentativas expiradas do cache.
     * Deve ser chamado periodicamente por um scheduler.
     */
    public void cleanupExpiredAttempts() {
        long currentTime = System.currentTimeMillis();
        loginAttempts.entrySet().removeIf(entry -> {
            long timeSinceLastAttempt = currentTime - entry.getValue().getLastAttemptTime();
            return timeSinceLastAttempt > LOCK_TIME_DURATION;
        });
    }

    /**
     * Classe interna para armazenar informações de tentativas de login.
     */
    private static class LoginAttempt {
        private int attempts = 0;
        private long lastAttemptTime = 0;

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }

        public void setLastAttemptTime(long lastAttemptTime) {
            this.lastAttemptTime = lastAttemptTime;
        }
    }
}
