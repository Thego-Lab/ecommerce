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

    @RequestMapping(value = "/chat", produces = "text/plain;charset=UTF-8")
    public Flux<String> chat(@RequestParam String prompt, HttpServletRequest request) {
        tryAuthenticate(request);
        return serviceChatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .doFinally(s -> UserHolder.removeUser());
    }

    private void tryAuthenticate(HttpServletRequest request) {
        String token = request.getHeader("X-Token");
        if (token == null || token.isEmpty()) return;
        try {
            if (jwtUtil.isExpired(token, jwtProperties.getSecretKey())) return;
            Long userId = jwtUtil.getUserIdFromToken(token, jwtProperties.getSecretKey());
            UserDTO userDTO = new UserDTO();
            userDTO.setId(userId);
            userDTO.setNickname("");
            UserHolder.saveUser(userDTO);
        } catch (Exception ignored) {}
    }
}
