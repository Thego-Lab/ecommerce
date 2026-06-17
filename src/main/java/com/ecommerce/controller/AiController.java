package com.ecommerce.controller;

import com.ecommerce.dto.UserDTO;
import com.ecommerce.utils.JwtUtil;
import com.ecommerce.utils.UserHolder;
import com.ecommerce.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 智能客服接口（公开访问，登录后能识别用户身份）
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient serviceChatClient;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;

    public AiController(ChatClient serviceChatClient, JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.serviceChatClient = serviceChatClient;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }

    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(@RequestParam String prompt, HttpServletRequest request) {
        // 尝试解析登录态：有有效 token 就注入用户，没有也正常服务
        tryAuthenticate(request);

        try {
            return serviceChatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();
        } finally {
            UserHolder.removeUser();
        }
    }

    private void tryAuthenticate(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        if (token == null || token.isEmpty()) return;

        try {
            if (jwtUtil.isExpired(token, jwtProperties.getSecretKey())) return;

            Long userId = jwtUtil.getUserIdFromToken(token, jwtProperties.getSecretKey());
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setNickname(""); // AI 场景不需要昵称
            UserHolder.saveUser(userDTO);
        } catch (Exception ignored) {
            // Token 无效，当作游客
        }
    }
}
