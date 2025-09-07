package com.burger.smartblog.ai.chat;

import com.burger.smartblog.model.entity.Article;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ChatService {

    @Resource
    private ChatClient chatClient;

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

}
