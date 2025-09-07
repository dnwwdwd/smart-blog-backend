package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.mapper.ChatMessageMapper;
import com.burger.smartblog.model.entity.ChatMessage;
import com.burger.smartblog.service.ChatConversationService;
import com.burger.smartblog.service.ChatMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
 * @createDate 2025-09-07 11:28:16
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Resource
    private ChatConversationService chatConversationService;

    @Override
    public List<ChatMessage> getChatMessages(Long conversationId) {
        return this.lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .list();
    }
}




