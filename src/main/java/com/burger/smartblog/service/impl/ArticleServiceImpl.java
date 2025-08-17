package com.burger.smartblog.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.enums.ArticleStatusEnum;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.mapper.ArticleMapper;
import com.burger.smartblog.model.dto.article.ArticlePublishRequest;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.*;
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
    @Lazy
    private CommentService commentService;

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
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
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
            List<Comment> comments = commentService.getCommentsByArticleId(article.getId());
            articleVo.setComments(comments);
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
        if (CollectionUtils.isEmpty(articleIds)) {
            return new ArrayList<>();
        }
        return this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .orderByAsc(Article::getPublishedTime)
                .list();
    }

    @Override
    public List<Article> getArticlesByColumnId(Long columnId) {
        if (columnId == null) {
            return new ArrayList<>();
        }
        List<Long> articleIds = articleColumnService.lambdaQuery()
                .eq(ArticleColumn::getColumnId, columnId)
                .list().stream().map(ArticleColumn::getArticleId).toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return new ArrayList<>();
        }
        return this.lambdaQuery()
                .orderByAsc(Article::getPublishedTime)
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .list();
    }

    @Override
    public ArticleVo getArticleVoById(Long articleId) {
        if (articleId == null) {
            return null;
        }
        Article article = this.getById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }
        ArticleVo articleVo = new ArticleVo();
        BeanUtils.copyProperties(article, articleVo);
        List<String> tagNames = tagService.getTagsByArticleId(article.getId()).stream().map(Tag::getName).toList();
        articleVo.setTags(tagNames);
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        articleVo.setComments(comments);
        List<Column> columns = columnService.getColumnsByArticleId(article.getId());
        articleVo.setColumns(columns);
        return articleVo;
    }

    @Override
    public Page<ArticleVo> getArticlePageByColumnId(Long columnId, ArticleRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        if (columnId == null) {
            return new Page<>();
        }
        Page<ArticleColumn> articleColumnPage = articleColumnService.lambdaQuery()
                .eq(ArticleColumn::getColumnId, columnId)
                .orderByDesc(ArticleColumn::getCreateTime)
                .page(new Page<>(current, pageSize));
        List<Long> articleIds = articleColumnPage.getRecords().stream().map(ArticleColumn::getId).toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return new Page<>();
        }
        List<Article> articles = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .orderByAsc(Article::getPublishedTime)
                .list();
        List<ArticleVo> articleVos = getArticleVos(articles);
        Page<ArticleVo> articleVoPage = new Page<>(current, pageSize, articleVos.size());
        articleVoPage.setRecords(articleVos);
        return articleVoPage;
    }

    @Override
    public Page<ArticleVo> getArticlePageByTagId(Long tagId, ArticleRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        if (tagId == null) {
            return new Page<>();
        }
        Page<ArticleTag> articleTagPage = articleTagService.lambdaQuery()
                .eq(ArticleTag::getTagId, tagId)
                .orderByDesc(ArticleTag::getCreateTime)
                .page(new Page<>(current, pageSize));
        List<Long> articleIds = articleTagPage.getRecords().stream().map(ArticleTag::getId).toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return new Page<>();
        }
        List<Article> articles = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .orderByAsc(Article::getPublishedTime)
                .list();
        List<ArticleVo> articleVos = getArticleVos(articles);
        Page<ArticleVo> articleVoPage = new Page<>(current, pageSize, articleVos.size());
        articleVoPage.setRecords(articleVos);
        return articleVoPage;
    }

    @Override
    public Page<ArticleVo> getAllArticles(ArticleRequest request) {
        String title = request.getTitle();
        Page<Article> articlePage = this.lambdaQuery()
                .like(StringUtils.isNotBlank(title), Article::getTitle, title)
                .page(new Page<>(request.getCurrent(), request.getPageSize()));
        List<ArticleVo> articleVos = getArticleVos(articlePage.getRecords());
        Page<ArticleVo> articleVoPage = new Page<>(request.getCurrent(), request.getPageSize(), articlePage.getTotal());
        articleVoPage.setRecords(articleVos);
        return articleVoPage;
    }

    private List<ArticleVo> getArticleVos(List<Article> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return new ArrayList<>();
        }
        return articles.stream().map(article -> getArticleVoById(article.getId())).toList();
    }

}




