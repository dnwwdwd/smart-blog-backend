package com.burger.smartblog.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.enums.ArticleStatusEnum;
import com.burger.smartblog.enums.UploadStatusEnum;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.mapper.ArticleMapper;
import com.burger.smartblog.model.dto.article.ArticleDto;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.*;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.model.vo.CommentVo;
import com.burger.smartblog.service.*;
import com.burger.smartblog.service.chat.ChatService;
import com.burger.smartblog.service.chat.RagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Map;
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
    @Lazy
    private ArticleService self;

    @Resource
    private ChatService chatService;

    @Resource(name = "articleGeneratorExecutor")
    private java.util.concurrent.Executor articleGeneratorExecutor;

    @Resource
    private RagService ragService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDto articleDto) {
        Article article = new Article();
        BeanUtils.copyProperties(articleDto, article);
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
                .orderByDesc(Article::getUpdateTime)
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
    public List<Long> batchUpload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }

        // 1) 先为每个文件创建占位记录，返回 ID 列表
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Article placeholder = new Article();
            placeholder.setUploadStatus(UploadStatusEnum.UPLOADING.getCode());
            placeholder.setStatus(ArticleStatusEnum.DRAFT.getCode());
            this.save(placeholder);
            ids.add(placeholder.getId());
        }

        // 2) 读取字节数组 + 原始文件名，开启异步处理：按顺序映射到对应的占位 ID
        byte[][] byteList = Arrays.stream(files)
                .map(file -> {
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(byte[][]::new);

        String[] fileNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String name = files[i].getOriginalFilename();
            fileNames[i] = StringUtils.isNotBlank(name) ? name : ("upload-" + i + ".txt");
        }

        CompletableFuture.runAsync(() -> {
            try {
                self.batchUploadAndSaveArticles(byteList, ids, fileNames);
            } catch (Exception e) {
                log.error("批量上传异步任务执行失败", e);
            }
        }, articleGeneratorExecutor);

        return ids;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchUploadAndSaveArticles(byte[][] byteList, List<Long> ids, String[] fileNames) {
        log.info("批量上传文章开始，共 {} 篇", byteList == null ? 0 : byteList.length);
        if (byteList == null || byteList.length == 0 || ids == null || ids.isEmpty()) {
            return;
        }
        int total = Math.min(byteList.length, ids.size());
        for (int i = 0; i < total; i++) {
            byte[] bytes = byteList[i];
            Long articleId = ids.get(i);
            String originalName = (fileNames != null && fileNames.length > i && StringUtils.isNotBlank(fileNames[i]))
                    ? fileNames[i] : ("upload-" + i + ".txt");

            try {
                // 解析内容并生成元数据
                String articleContent = this.bytesToString(bytes);
                Article toGen = new Article();
                toGen.setContent(articleContent);
                Article generated = chatService.generateArticleMetaData(toGen);

                // 标题唯一性校验：若已存在同名文章，则标记失败并跳过
                String genTitle2 = generated.getTitle();
                if (StringUtils.isNotBlank(genTitle2) && this.lambdaQuery().eq(Article::getTitle, genTitle2).count() > 0) {
                    Article dupUpdate = new Article();
                    dupUpdate.setId(articleId);
                    dupUpdate.setContent(articleContent);
                    dupUpdate.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                    this.updateById(dupUpdate);
                    log.warn("批量上传：检测到重复标题，已拒绝上传并标记失败，title={}", genTitle2);
                    continue;
                }

                // 计算阅读时间
                int readTime = (articleContent != null && articleContent.length() >= 100)
                        ? articleContent.length() / 100 : 1;

                // 将生成结果回写到同一条记录，并标记成功
                Article toUpdate = new Article();
                toUpdate.setId(articleId);
                toUpdate.setTitle(FileUtil.mainName(originalName));
                toUpdate.setContent(articleContent);
                toUpdate.setExcerpt(generated.getExcerpt());
                toUpdate.setCoverImage(generated.getCoverImage());
                toUpdate.setSeoTitle(generated.getSeoTitle());
                toUpdate.setSeoDescription(generated.getSeoDescription());
                toUpdate.setSeoKeywords(generated.getSeoKeywords());
                toUpdate.setReadTime(readTime);
                toUpdate.setStatus(ArticleStatusEnum.PUBLISHED.getCode());
                toUpdate.setUploadStatus(UploadStatusEnum.SUCCESS.getCode());
                this.updateById(toUpdate);

                // 将文件向量存储到知识库（携带原始文件名）
                ragService.storeFileToDashScopeCloudStore(bytes, originalName);

            } catch (Exception ex) {
                log.error("单篇文章上传处理失败，articleId={}。将标记为失败", articleId, ex);
                Article failUpdate = new Article();
                failUpdate.setId(articleId);
                failUpdate.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                this.updateById(failUpdate);
            }
        }
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

    }

    @Override
    public Map<Long, Integer> getUploadStatuses(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<Article> list = this.lambdaQuery().in(Article::getId, ids).list();
        return list.stream().collect(Collectors.toMap(Article::getId, Article::getUploadStatus));
    }

    @Override
    public void retryUpload(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id 不能为空");
        }
        Article db = this.getById(id);
        if (db == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章不存在");
        }
        String content = db.getContent();
        if (StringUtils.isBlank(content)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "原文内容为空，无法重试");
        }
        // 标记为处理中
        Article updating = new Article();
        updating.setId(id);
        updating.setUploadStatus(UploadStatusEnum.UPLOADING.getCode());
        this.updateById(updating);

        CompletableFuture.runAsync(() -> {
            try {
                Article toGen = new Article();
                toGen.setContent(content);
                Article generated = chatService.generateArticleMetaData(toGen);

                // 标题唯一性校验（重试上传）：若已存在同名文章，则标记失败并退出
                String retryTitle = generated.getTitle();
                if (StringUtils.isNotBlank(retryTitle) && this.lambdaQuery().eq(Article::getTitle, retryTitle).count() > 0) {
                    Article dup = new Article();
                    dup.setId(id);
                    dup.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                    this.updateById(dup);
                    log.warn("重试上传：检测到重复标题，已拒绝上传并标记失败，title={}", retryTitle);
                    return;
                }

                int readTime = (content.length() >= 100) ? content.length() / 100 : 1;
                Article toUpdate = new Article();
                toUpdate.setId(id);
                toUpdate.setTitle(generated.getTitle());
                toUpdate.setExcerpt(generated.getExcerpt());
                toUpdate.setCoverImage(generated.getCoverImage());
                toUpdate.setSeoTitle(generated.getSeoTitle());
                toUpdate.setSeoDescription(generated.getSeoDescription());
                toUpdate.setSeoKeywords(generated.getSeoKeywords());
                toUpdate.setReadTime(readTime);
                toUpdate.setStatus(ArticleStatusEnum.PUBLISHED.getCode());
                toUpdate.setUploadStatus(UploadStatusEnum.SUCCESS.getCode());
                this.updateById(toUpdate);
            } catch (Exception ex) {
                log.error("重试处理失败，articleId={}。将标记为失败", id, ex);
                Article failUpdate = new Article();
                failUpdate.setId(id);
                failUpdate.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                this.updateById(failUpdate);
            }
        }, articleGeneratorExecutor);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteArticle(Long articleId) {
        if (articleId == null) {
            return;
        }
        // 删除文章与关联关系、评论
        // 1) 删除标签关联
        articleTagService.remove(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        // 2) 删除专栏关联
        articleColumnService.remove(new LambdaQueryWrapper<ArticleColumn>().eq(ArticleColumn::getArticleId, articleId));
        // 3) 删除评论
        commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, articleId));
        // 4) 删除文章本身
        this.removeById(articleId);
    }
}




