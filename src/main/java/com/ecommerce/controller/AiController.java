package com.ecommerce.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 智能客服接口（流式返回，无需登录）
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient serviceChatClient;

    public AiController(ChatClient serviceChatClient) {
        this.serviceChatClient = serviceChatClient;
    }

    /**
     * 智能客服对话
     * @param prompt 用户问题
     * @return 流式返回 AI 回答
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(@RequestParam String prompt) {
        return serviceChatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }
}
