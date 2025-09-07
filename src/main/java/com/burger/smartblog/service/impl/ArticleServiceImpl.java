package com.burger.smartblog.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.ai.chat.ChatService;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.enums.ArticleStatusEnum;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.mapper.ArticleMapper;
import com.burger.smartblog.model.dto.article.ArticleDto;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.*;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.model.vo.CommentVo;
import com.burger.smartblog.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author hejiajun
 * @description 针对表【article】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:32
 */
@Service
@Slf4j
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

    @Resource
    private ChatClient chatClient;

    @Resource
    @Lazy
    private ArticleService self;

    @Resource
    private ChatService chatService;

    @Resource(name = "articleGeneratorExecutor")
    private java.util.concurrent.Executor articleGeneratorExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDto articleDto) {
        Article article = new Article();
        BeanUtils.copyProperties(articleDto, article);
        // todo 如果 excerpt 为空 AI 生成
        String content = articleDto.getContent();
        // 根据 content 字数设置阅读时间
        if (content.length() < 100) {
            article.setReadTime(1);
        } else {
            int readTime = content.length() / 100;
            article.setReadTime(readTime);
        }
        Article tmpArticle = article;
        article = chatService.generateArticleMetaData(tmpArticle);
        // 保存文章
        this.save(article);
        if (article.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文章保存失败");
        }
        List<String> tags = articleDto.getTags();
        // 标签
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
        List<Long> columnIds = articleDto.getColumnIds();
        // 专栏
        for (Long columnId : columnIds) {
            ArticleColumn articleColumn = new ArticleColumn();
            articleColumn.setArticleId(article.getId());
            articleColumn.setColumnId(columnId);
            articleColumnService.save(articleColumn);
        }
        // todo 文件向量化存储
        return article.getId();
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
            List<CommentVo> comments = commentService.getCommentsByArticleId(article.getId());
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
        List<CommentVo> comments = commentService.getCommentsByArticleId(articleId);
        articleVo.setComments(comments);
        List<Column> columns = columnService.getColumnsByArticleId(article.getId());
        articleVo.setColumns(columns);
        String seoKeywords = article.getSeoKeywords();
        if (StringUtils.isNotBlank(seoKeywords)) {
            articleVo.setSeoKeywords(JSONUtil.toList(seoKeywords, String.class));
        }
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
        List<Long> articleIds = articleColumnPage.getRecords().stream().map(ArticleColumn::getArticleId).toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return new Page<>();
        }
        List<Article> articles = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .orderByAsc(Article::getPublishedTime)
                .list();
        List<ArticleVo> articleVos = getArticleVos(articles);
        Page<ArticleVo> articleVoPage = new Page<>(current, pageSize, articleColumnPage.getTotal());
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
        List<Long> articleIds = articleTagPage.getRecords().stream().map(ArticleTag::getArticleId).toList();
        if (CollectionUtils.isEmpty(articleIds)) {
            return new Page<>();
        }
        List<Article> articles = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, articleIds)
                .orderByAsc(Article::getPublishedTime)
                .list();
        List<ArticleVo> articleVos = getArticleVos(articles);
        Page<ArticleVo> articleVoPage = new Page<>(current, pageSize, articleTagPage.getTotal());
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

    @Override
    public void batchUpload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        byte[][] byteList = Arrays.stream(files)
                .map(file -> {
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(byte[][]::new);
        // 使用 CompletableFuture.runAsync 异步上传，并在失败时记录日志且确保事务回滚
        CompletableFuture.runAsync(() -> {
            try {
                self.batchUploadAndSaveArticles(byteList);
            } catch (Exception e) {
                log.error("批量上传文章失败，已回滚事务", e);
                throw e; // 继续抛出以触发事务回滚
            }
        }, articleGeneratorExecutor);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUploadAndSaveArticles(byte[][] byteList) {
        log.info("批量上传文章开始");
        List<Article> articles = new ArrayList<>();
        for (byte[] bytes : byteList) {
            // todo 文件大小限制
            String articleContent = this.bytesToString(bytes);
            // todo 文件内容判断重复
            Article article = new Article();
            Article tmpArticle = article;
            article.setContent(articleContent);
            article = chatService.generateArticleMetaData(tmpArticle);
            article.setStatus(ArticleStatusEnum.PUBLISHED.getCode());
            if (articleContent.length() < 100) {
                article.setReadTime(1);
            } else {
                int readTime = articleContent.length() / 100;
                article.setReadTime(readTime);
            }
            articles.add(article);
        }
        this.saveBatch(articles);
        log.info("批量上传文章结束");
    }

    private List<ArticleVo> getArticleVos(List<Article> articles) {
        if (CollectionUtils.isEmpty(articles)) {
            return new ArrayList<>();
        }
        return articles.stream().map(article -> getArticleVoById(article.getId())).toList();
    }

    private String bytesToString(byte[] bytes) {
        // 处理 UTF-8 BOM
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public boolean generateArticleMetaData(Long articleId) {
        if (articleId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Article article = this.getById(articleId);
        Article tmpArticle = article;
        article = chatService.generateArticleMetaData(tmpArticle);
        return this.updateById(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleDto articleDto) {
        if (articleDto == null || articleDto.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章 id 不能为空");
        }
        Article dbArticle = this.getById(articleDto.getId());
        if (dbArticle == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }
        // 基础字段
        dbArticle.setTitle(articleDto.getTitle());
        dbArticle.setContent(articleDto.getContent());
        dbArticle.setExcerpt(articleDto.getExcerpt());
        dbArticle.setCoverImage(articleDto.getCoverImage());
        dbArticle.setSeoTitle(articleDto.getSeoTitle());
        dbArticle.setSeoDescription(articleDto.getSeoDescription());
        if (CollectionUtils.isNotEmpty(articleDto.getSeoKeywords())) {
            dbArticle.setSeoKeywords(JSONUtil.toJsonStr(articleDto.getSeoKeywords()));
        }
        dbArticle.setStatus(articleDto.getStatus());
        // 更新阅读时间
        String content = articleDto.getContent();
        if (StringUtils.isNotBlank(content)) {
            dbArticle.setReadTime(content.length() < 100 ? 1 : content.length() / 100);
        }
        this.updateById(dbArticle);

        // 更新标签关联：先删后增
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, dbArticle.getId()));
        if (CollectionUtils.isNotEmpty(articleDto.getTags())) {
            for (String tagName : articleDto.getTags()) {
                Tag tag = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagName));
                if (tag == null) {
                    tag = new Tag();
                    tag.setName(tagName);
                    tagService.save(tag);
                }
                ArticleTag at = new ArticleTag();
                at.setArticleId(dbArticle.getId());
                at.setTagId(tag.getId());
                articleTagService.save(at);
            }
        }

        // 更新专栏关联：先删后增
        articleColumnService.remove(new LambdaQueryWrapper<ArticleColumn>()
                .eq(ArticleColumn::getArticleId, dbArticle.getId()));
        if (CollectionUtils.isNotEmpty(articleDto.getColumnIds())) {
            for (Long columnId : articleDto.getColumnIds()) {
                ArticleColumn ac = new ArticleColumn();
                ac.setArticleId(dbArticle.getId());
                ac.setColumnId(columnId);
                articleColumnService.save(ac);
            }
        }

        // todo 文件向量化存储
    }

}




