package com.ecommerce.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI 配置 — ChatClient Bean
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel model, EcommerceTools tools) {
        return ChatClient.builder(model)
                .defaultSystem(AiSystemPrompt.CUSTOMER_SERVICE)
                .defaultTools(tools)                        // Function Calling
                .defaultAdvisors(new SimpleLoggerAdvisor()) // 控制台打印日志
                .build();
    }
}
