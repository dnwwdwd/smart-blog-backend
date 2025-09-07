package com.burger.smartblog.service;

import com.burger.smartblog.model.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author hejiajun
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service
* @createDate 2025-09-07 11:28:16
*/
public interface ChatMessageService extends IService<ChatMessage> {

    List<ChatMessage> getChatMessages(Long conversationId);

}
