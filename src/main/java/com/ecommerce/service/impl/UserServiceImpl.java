package com.ecommerce.service.impl;

import com.ecommerce.config.JwtProperties;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.LoginResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.UserService;
import com.ecommerce.utils.JwtUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ecommerce.utils.RedisConstants.LOGIN_USER_KEY;
import static com.ecommerce.utils.RedisConstants.LOGIN_USER_TTL;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private JwtProperties jwtProperties;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void register(RegisterRequest request) {
        User exist = userMapper.selectByUsername(request.getUsername());
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole("buyer");  // 默认买家
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 校验用户名密码
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 生成 JWT（无状态，不查 DB）
        String token = jwtUtil.generateToken(
                user.getId(),
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl()
        );

        // 3. 用户信息存入 Redis（用于拦截器快速校验 + 支持登出失效）
        String key = LOGIN_USER_KEY + user.getId();
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", user.getId().toString());
        userMap.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        userMap.put("role", user.getRole() != null ? user.getRole() : "buyer");
        userMap.put("token", token);
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 4. 返回
        LoginResponse response = new LoginResponse();
        response.setUserId(user.getId());
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole() != null ? user.getRole() : "buyer");
        return response;
    }
}
