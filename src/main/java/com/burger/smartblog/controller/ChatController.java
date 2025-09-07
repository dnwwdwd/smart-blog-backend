package com.burger.smartblog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口", description = "聊天接口")
@Slf4j
public class ChatController {

    @Resource
    private ChatClient chatClient;

    @Resource
    private RetrievalAugmentationAdvisor smartBlogRagAdvisor;

    @PostMapping("/completion")
    @Operation(summary = "聊天")
    public String completion(@RequestParam String message) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                // 应用增强检索服务（云知识库服务）
                .advisors(smartBlogRagAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
