package com.burger.smartblog.controller;

import com.burger.smartblog.service.chat.ChatService;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.entity.ChatConversation;
import com.burger.smartblog.model.entity.ChatMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口", description = "聊天接口")
@Slf4j
public class ChatController {

    @Resource
    private ChatService chatService;

    @PostMapping(
            value = "/completion",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    @Operation(summary = "聊天")
    public Flux<String> completion(@RequestParam String message, @RequestParam Long conversationId) {
        return chatService.completion(message, conversationId);
    }

    @GetMapping("/history/{conversationId}")
    @Operation(summary = "获取聊天历史")
    public BaseResponse<List<ChatMessage>> getChatHistory(@PathVariable Long conversationId) {
        return ResultUtils.success(chatService.getChatHistory(conversationId));
    }

    @PostMapping("/conversation/add")
    @Operation(summary = "新增聊天会话")
    public BaseResponse<Long> addChatConversation() {
        return ResultUtils.success(chatService.addChatConversation());
    }

    @GetMapping("/conversation/list")
    @Operation(summary = "获取聊天会话列表")
    public BaseResponse<List<ChatConversation>> getChatConversationList() {
        return ResultUtils.success(chatService.getChatConversationList());
    }

    @PostMapping("/conversation/delete/{conversationId}")
    @Operation(summary = "删除聊天会话")
    public BaseResponse<Boolean> deleteChatConversation(@PathVariable Long conversationId) {
        return ResultUtils.success(chatService.deleteChatConversation(conversationId));
    }

    @PostMapping("/conversation/update")
    @Operation(summary = "更新聊天会话")
    public BaseResponse<Boolean> updateChatConversation(@RequestBody ChatConversation chatConversation) {
        return ResultUtils.success(chatService.updateChatConversation(chatConversation));
    }


}
