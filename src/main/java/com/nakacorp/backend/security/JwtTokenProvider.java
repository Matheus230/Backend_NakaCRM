package com.nakacorp.backend.security;

import com.nakacorp.backend.model.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;

    @Value("${jwt.expiration:86400000}") // 24 hours default
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration:604800000}") // 7 days default
    private long refreshExpirationInMs;

    public JwtTokenProvider(@Value("${jwt.secret:mySecretKey}") String jwtSecret) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", usuario.getId());
        claims.put("email", usuario.getEmail());
        claims.put("nome", usuario.getNome());
        claims.put("tipoUsuario", usuario.getTipoUsuario().toString());
        claims.put("ativo", usuario.getAtivo());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(usuario.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Usuario usuario) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpirationInMs);

        return Jwts.builder()
                .setSubject(usuario.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("tokenType", "refresh")
                .claim("userId", usuario.getId())
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }


    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public String getUserRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("tipoUsuario", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty");
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public long getExpirationTimeInSeconds() {
        return jwtExpirationInMs / 1000;
    }

    public long getRemainingTimeInSeconds(String token) {
        Date expiration = getExpirationDateFromToken(token);
        long remainingTime = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remainingTime / 1000);
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "refresh".equals(claims.get("tokenType"));
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> getTokenInfo(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Map<String, Object> info = new HashMap<>();
        info.put("subject", claims.getSubject());
        info.put("issuedAt", LocalDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault()));
        info.put("expiration", LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault()));
        info.put("userId", claims.get("userId"));
        info.put("email", claims.get("email"));
        info.put("nome", claims.get("nome"));
        info.put("tipoUsuario", claims.get("tipoUsuario"));
        info.put("ativo", claims.get("ativo"));

        return info;
    }
}