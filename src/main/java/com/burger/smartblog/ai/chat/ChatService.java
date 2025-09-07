package com.burger.smartblog.ai.chat;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.burger.smartblog.ai.chatmemory.ChatMessageMemory;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.entity.ChatConversation;
import com.burger.smartblog.model.entity.ChatMessage;
import com.burger.smartblog.service.ChatConversationService;
import com.burger.smartblog.service.ChatMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatService {

    @Resource
    private ChatClient chatClient;

    @Resource
    private RetrievalAugmentationAdvisor smartBlogRagAdvisor;

    @Resource
    private ChatMessageMemory chatMessageMemory;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatConversationService chatConversationService;

    public Article generateArticleMetaData(Article tmpArticle) {
        log.info("开始生成文章：{} 元数据", tmpArticle);
        Article article = chatClient.prompt()
                .user(u -> u.text("根据这个文章的内容：{article} ，生成对应的 title（18字以内）、 excerpt（50字以内）" +
                                "、seoTitle（15字以内）、seoDescription（20字以内）、seoKeywords（3~5 个即可，且格式为json数组）字段，" +
                                "并且确保 title、excerpt、seoTitle、seoDescription、seoKeywords 这 5 个字段的内容必须是中文，注意其他的字段的内容不要变动")
                        .params(Map.of("article", tmpArticle))).call().entity(Article.class);
        log.info("文章：{} 元数据生成完成", tmpArticle);
        return article;
    }

    public Flux<String> completion(String message, Long conversationId) {
        return chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(chatMessageMemory).build())
                .advisors(smartBlogRagAdvisor)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream().content();
    }

    public List<ChatMessage> getChatHistory(Long conversationId) {
        return chatMessageService.getChatMessages(conversationId);
    }

    public Long addChatConversation() {
        ChatConversation conversation = new ChatConversation();
        conversation.setName("暂无标题");
        conversation.setCreateBy(StpUtil.getLoginIdAsLong());
        chatConversationService.save(conversation);
        return conversation.getId();
    }

    public List<ChatConversation> getChatConversationList() {
        return chatConversationService.lambdaQuery()
                .eq(ChatConversation::getCreateBy, StpUtil.getLoginIdAsLong())
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteChatConversation(Long conversationId) {
        boolean isSuccess = chatConversationService.removeById(conversationId);
        if (isSuccess) {
            chatMessageService.remove(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getConversationId, conversationId));
        }
        return isSuccess;
    }

    public Boolean updateChatConversation(ChatConversation chatConversation) {
        return chatConversationService.updateById(chatConversation);
    }
}
