package com.burger.smartblog.ai.config;

import com.burger.smartblog.ai.advisor.ChatLoggerAdvisor;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Resource
    private ChatModel chatModel;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel).defaultAdvisors(new ChatLoggerAdvisor()).build();
    }

}
