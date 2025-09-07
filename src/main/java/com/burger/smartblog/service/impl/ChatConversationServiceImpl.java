package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.model.entity.ChatConversation;
import com.burger.smartblog.service.ChatConversationService;
import com.burger.smartblog.mapper.ChatConversationMapper;
import org.springframework.stereotype.Service;

/**
* @author hejiajun
* @description 针对表【chat_conversation(聊天对话)】的数据库操作Service实现
* @createDate 2025-09-07 11:28:16
*/
@Service
public class ChatConversationServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation>
    implements ChatConversationService{

}




