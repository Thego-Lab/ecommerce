package com.ecommerce.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String secretKey, long ttl) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttl))
                .signWith(key)
                .compact();
    }

    /**
     * 解析 JWT，返回 Claims
     */
    public Claims parseToken(String token, String secretKey) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserIdFromToken(String token, String secretKey) {
        Claims claims = parseToken(token, secretKey);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isExpired(String token, String secretKey) {
        try {
            Claims claims = parseToken(token, secretKey);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
