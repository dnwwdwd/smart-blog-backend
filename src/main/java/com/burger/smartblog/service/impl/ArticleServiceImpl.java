package com.burger.smartblog.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.cglib.CglibUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.mapper.ArticleMapper;
import com.burger.smartblog.model.dto.article.ArticlePublishRequest;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.entity.ArticleColumn;
import com.burger.smartblog.model.entity.ArticleTag;
import com.burger.smartblog.model.entity.Tag;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.service.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hejiajun
 * @description 针对表【article】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:32
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article>
        implements ArticleService {

    @Resource
    @Lazy
    private ColumnService columnService;

    @Resource
    private ArticleColumnService articleColumnService;

    @Resource
    @Lazy
    private TagService tagService;

    @Resource
    private ArticleTagService articleTagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(ArticlePublishRequest articlePublishRequest) {
        Article article = new Article();
        BeanUtils.copyProperties(articlePublishRequest, article);
        // todo 如果 excerpt 为空 AI 生成
        String content = articlePublishRequest.getContent();
        // 根据 content 字数设置阅读时间
        if (StringUtils.isNotBlank(content)) {
            if (content.length() < 100) {
                article.setReadTime(1);
            } else {
                int readTime = content.length() / 100;
                article.setReadTime(readTime);
            }
        }
        List<String> tags = articlePublishRequest.getTags();
        // 标签
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                Tag tagEntity = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tag));
                if (tagEntity == null) {
                    tagEntity = new Tag();
                    tagEntity.setName(tag);
                    tagService.save(tagEntity);
                }
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagEntity.getId());
                articleTagService.save(articleTag);
            }
        }
        List<Long> columnIds = articlePublishRequest.getColumnIds();
        // 专栏
        if (CollectionUtils.isNotEmpty(columnIds)) {
            for (Long columnId : columnIds) {
                ArticleColumn articleColumn = new ArticleColumn();
                articleColumn.setArticleId(article.getId());
                articleColumn.setColumnId(columnId);
                articleColumnService.save(articleColumn);
            }
        }
        this.save(article);
    }

    @Override
    public Page<ArticleVo> getArticlePage(ArticleRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        Page<Article> articlePage = this.lambdaQuery()
                .like(StringUtils.isNotBlank(request.getTitle()), Article::getTitle, request.getTitle())
                .orderByDesc(Article::getPublishedTime)
                .page(new Page<>(current, pageSize));
        List<ArticleVo> articleVoList = articlePage.getRecords().stream().map(article -> {
            ArticleVo articleVo = new ArticleVo();
            BeanUtils.copyProperties(article, articleVo);
            String seoKeywords = article.getSeoKeywords();
            if (StringUtils.isNotBlank(seoKeywords)) {
                articleVo.setSeoKeywords(JSONUtil.toList(article.getSeoKeywords(), String.class));
            }
            List<String> tagNames = tagService.getTagsByArticleId(article.getId()).stream().map(Tag::getName).toList();
            articleVo.setTags(tagNames);
            // todo 查询 comments
            return articleVo;
        }).collect(Collectors.toList());
        Page<ArticleVo> articleVoPage = new Page<>(current, pageSize, articlePage.getTotal());
        articleVoPage.setRecords(articleVoList);
        return articleVoPage;
    }

    @Override
    public List<Article> getArticlesByTagId(Long tagId) {
        if (tagId == null) {
            return new ArrayList<>();
        }
        List<Long> articleIds = articleTagService.lambdaQuery()
                .eq(ArticleTag::getTagId, tagId)
                .list().stream().map(ArticleTag::getArticleId).toList();
        return this.listByIds(articleIds);
    }

    @Override
    public List<Article> getArticlesByColumnId(Long columnId) {
        if (columnId == null) {
            return new ArrayList<>();
        }
        List<Long> articleIds = articleColumnService.lambdaQuery()
                .eq(ArticleColumn::getColumnId, columnId)
                .list().stream().map(ArticleColumn::getArticleId).toList();
        return this.listByIds(articleIds);
    }

}




