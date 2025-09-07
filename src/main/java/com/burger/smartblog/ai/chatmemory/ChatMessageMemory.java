package com.burger.smartblog.ai.chatmemory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.burger.smartblog.enums.MessageRoleEnum;
import com.burger.smartblog.model.entity.ChatMessage;
import com.burger.smartblog.service.ChatMessageService;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageMemory implements ChatMemory {

    @Resource
    @Lazy
    private ChatMessageService chatMessageService;

    @Override
    public void add(String conversationId, Message message) {
        ChatMessage chatMessage = this.transferToChatMessage(message);
        chatMessage.setConversationId(conversationId);
        chatMessageService.save(chatMessage);
    }

    @Override
    public List<Message> get(String conversationId) {
        List<ChatMessage> chatMessages = chatMessageService.lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .list();
        if (CollectionUtils.isEmpty(chatMessages)) {
            return Collections.emptyList();
        }
        return chatMessages.stream()
                .map(this::transferToMessage)
                .collect(Collectors.toList());
    }

    private Message transferToMessage(ChatMessage chatMessage) {
        String metadata = chatMessage.getMetadata();
        Gson gson = new Gson();
        return gson.fromJson(metadata, MessageRoleEnum.getByValue(chatMessage.getRole()).getClazz());
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<ChatMessage> chatMessages = messages.stream().map(message -> {
            ChatMessage chatMessage = this.transferToChatMessage(message);
            chatMessage.setConversationId(conversationId);
            return chatMessage;
        }).toList();
        chatMessageService.saveBatch(chatMessages);
    }

    @Override
    public void clear(String conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        chatMessageService.remove(wrapper);
    }

    private ChatMessage transferToChatMessage(Message message) {
        Gson gson = new Gson();
        ChatMessage chatMessage = new ChatMessage();
        String role = message.getMessageType().getValue();
        chatMessage.setRole(role);
        chatMessage.setMetadata(gson.toJson(message));
        return chatMessage;
    }

}
