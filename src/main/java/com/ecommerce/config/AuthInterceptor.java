package com.ecommerce.config;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.utils.JwtUtil;
import com.ecommerce.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ecommerce.utils.RedisConstants.*;

/**
 * JWT + Redis 认证拦截器
 * <p>
 * 请求 → 解析 JWT → Redis 取用户 → 校验 token 一致性 → 刷新 TTL → ThreadLocal
 * <p>
 * 零 DB 查询，纯 Redis 操作，性能远优于传统 UUID Token 方案
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行 OPTIONS 预检
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 取请求头中的 JWT
        String token = request.getHeader("X-Token");
        if (token == null || token.isEmpty()) {
            write401(response, "未登录");
            return false;
        }

        // 2. 校验 JWT 是否过期
        if (jwtUtil.isExpired(token, jwtProperties.getSecretKey())) {
            write401(response, "登录已过期，请重新登录");
            return false;
        }

        // 3. 从 JWT 中提取 userId
        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token, jwtProperties.getSecretKey());
        } catch (Exception e) {
            write401(response, "Token 无效");
            return false;
        }

        // 4. 从 Redis 获取用户信息
        String key = LOGIN_USER_KEY + userId;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        if (userMap.isEmpty()) {
            write401(response, "登录已过期，请重新登录");
            return false;
        }

        // 5. 校验 Redis 中存的 token 与请求带的 token 一致（防止旧 JWT 被复用）
        String storedToken = (String) userMap.get("token");
        if (!token.equals(storedToken)) {
            write401(response, "登录已过期，请重新登录");
            return false;
        }

        // 6. 刷新 Redis key 过期时间（活跃用户自动续期）
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 7. 封装 UserDTO 存入 ThreadLocal
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setNickname((String) userMap.get("nickname"));
        userDTO.setRole((String) userMap.getOrDefault("role", "buyer"));
        UserHolder.saveUser(userDTO);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 防止内存泄漏
        UserHolder.removeUser();
    }

    private void write401(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\",\"data\":null}");
    }
}
