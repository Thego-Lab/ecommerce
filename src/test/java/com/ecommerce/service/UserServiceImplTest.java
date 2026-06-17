package com.ecommerce.service;

import com.ecommerce.config.JwtProperties;
import com.ecommerce.dto.request.LoginRequest;
import com.ecommerce.dto.request.RegisterRequest;
import com.ecommerce.dto.response.LoginResponse;
import com.ecommerce.entity.User;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.mapper.UserMapper;
import com.ecommerce.service.impl.UserServiceImpl;
import com.ecommerce.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserMapper userMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private JwtProperties jwtProperties;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private HashOperations<String, Object, Object> hashOps;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecretKey()).thenReturn("test-key-32bytes-long-for-hs256!!");
        when(jwtProperties.getTtl()).thenReturn(1800000L);
        when(stringRedisTemplate.opsForHash()).thenReturn(hashOps);
    }

    // ==================== 注册 ====================

    @Test
    void register_shouldSucceed_whenUsernameNotExist() {
        when(userMapper.selectByUsername("alice")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        assertDoesNotThrow(() -> userService.register(req));
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldThrow_whenUsernameExists() {
        User exist = new User();
        exist.setUsername("alice");
        when(userMapper.selectByUsername("alice")).thenReturn(exist);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register(req));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userMapper, never()).insert(any());
    }

    // ==================== 登录 ====================

    @Test
    void login_shouldReturnToken_whenCredentialsCorrect() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("123456");
        user.setNickname("Alice");
        when(userMapper.selectByUsername("alice")).thenReturn(user);
        when(jwtUtil.generateToken(1L, jwtProperties.getSecretKey(), 1800000L))
                .thenReturn("jwt-token-xxx");

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("123456");

        LoginResponse resp = userService.login(req);

        assertNotNull(resp);
        assertEquals(1L, resp.getUserId());
        assertEquals("jwt-token-xxx", resp.getToken());
        assertEquals("alice", resp.getUsername());
        verify(hashOps).putAll(anyString(), anyMap());
        verify(stringRedisTemplate).expire(anyString(), eq(30L), eq(TimeUnit.MINUTES));
    }

    @Test
    void login_shouldThrow_whenPasswordWrong() {
        User user = new User();
        user.setId(1L);
        user.setPassword("right-password");
        when(userMapper.selectByUsername("alice")).thenReturn(user);

        LoginRequest req = new LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong-password");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(req));
        assertEquals("用户名或密码错误", ex.getMessage());
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyLong());
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        when(userMapper.selectByUsername("nobody")).thenReturn(null);

        LoginRequest req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.login(req));
        assertEquals("用户名或密码错误", ex.getMessage());
    }
}
