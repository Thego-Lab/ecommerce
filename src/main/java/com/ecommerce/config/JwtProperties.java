package com.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ecommerce.jwt")
public class JwtProperties {

    /** JWT 签名密钥（至少 256 位 = 32 字节） */
    private String secretKey = "ThisIsA32BytesLongSecretKeyForHS256";

    /** JWT 过期时间（毫秒） */
    private long ttl = 1800000; // 默认 30 分钟

    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
}
