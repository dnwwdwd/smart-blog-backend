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
import com.burger.smartblog.manager.UploadBatchManager;
import com.burger.smartblog.mapper.ArticleMapper;
import com.burger.smartblog.model.dto.article.ArticleDto;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.*;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.model.vo.CommentVo;
import com.burger.smartblog.model.vo.upload.UploadBatchFileVo;
import com.burger.smartblog.model.vo.upload.UploadBatchResponse;
import com.burger.smartblog.model.vo.upload.UploadProgressPayload;
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
import java.util.*;
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

    @Resource
    private UploadBatchManager uploadBatchManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(ArticleDto articleDto) {
        if (articleDto == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文章数据不能为空");
        }
        boolean isDraft = ArticleStatusEnum.DRAFT.getCode().equals(articleDto.getStatus());
        if (!isDraft) {
            validatePublishPayload(articleDto);
        }
        Article article = new Article();
        BeanUtils.copyProperties(articleDto, article);
        String content = Optional.ofNullable(articleDto.getContent()).orElse("");
        article.setContent(content);
        // 根据 content 字数设置阅读时间
        int readTime = StringUtils.isBlank(content) ? 0 : Math.max(1, content.length() / 100);
        article.setReadTime(readTime);
        String seoKeywordsJson = null;
        if (CollectionUtils.isNotEmpty(articleDto.getSeoKeywords())) {
            seoKeywordsJson = JSONUtil.toJsonStr(articleDto.getSeoKeywords());
            article.setSeoKeywords(seoKeywordsJson);
        }
        if (!isDraft) {
            article = chatService.generateArticleMetaData(article);
            if (StringUtils.isNotBlank(seoKeywordsJson) && StringUtils.isBlank(article.getSeoKeywords())) {
                article.setSeoKeywords(seoKeywordsJson);
            }
        }
        article.setContent(content);
        article.setReadTime(readTime);
        article.setStatus(articleDto.getStatus());
        // 保存文章
        this.save(article);
        if (article.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文章保存失败");
        }
        List<String> tags = Optional.ofNullable(articleDto.getTags()).orElse(Collections.emptyList())
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .toList();
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
        List<Long> columnIds = Optional.ofNullable(articleDto.getColumnIds()).orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
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

    private void validatePublishPayload(ArticleDto articleDto) {
        if (StringUtils.isBlank(articleDto.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
        }
        if (StringUtils.isBlank(articleDto.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }
        if (StringUtils.isBlank(articleDto.getCoverImage())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "封面不能为空");
        }
        if (CollectionUtils.isEmpty(articleDto.getTags())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少选择一个标签");
        }
        if (CollectionUtils.isEmpty(articleDto.getColumnIds())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少选择一个专栏");
        }
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
        this.lambdaUpdate()
                .setSql("views = views + 1")
                .eq(Article::getId, articleId)
                .update();
        article.setViews((article.getViews() == null ? 0 : article.getViews()) + 1);
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
        String keyword = StringUtils.isNotBlank(request.getKeyword()) ? request.getKeyword() : request.getTitle();
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getExcerpt, keyword));
        }
        if (request.getStatus() != null) {
            wrapper.eq(Article::getStatus, request.getStatus());
        }
        if (request.getPublishStartTime() != null) {
            wrapper.ge(Article::getPublishedTime, new Date(request.getPublishStartTime()));
        }
        if (request.getPublishEndTime() != null) {
            wrapper.le(Article::getPublishedTime, new Date(request.getPublishEndTime()));
        }

        if (request.getColumnId() != null) {
            List<Long> articleIds = articleColumnService.lambdaQuery()
                    .eq(ArticleColumn::getColumnId, request.getColumnId())
                    .list()
                    .stream()
                    .map(ArticleColumn::getArticleId)
                    .toList();
            if (CollectionUtils.isEmpty(articleIds)) {
                return emptyArticleVoPage(request.getCurrent(), request.getPageSize());
            }
            wrapper.in(Article::getId, articleIds);
        }

        if (request.getTagId() != null) {
            List<Long> articleIds = articleTagService.lambdaQuery()
                    .eq(ArticleTag::getTagId, request.getTagId())
                    .list()
                    .stream()
                    .map(ArticleTag::getArticleId)
                    .toList();
            if (CollectionUtils.isEmpty(articleIds)) {
                return emptyArticleVoPage(request.getCurrent(), request.getPageSize());
            }
            wrapper.in(Article::getId, articleIds);
        }

        wrapper.orderByDesc(Article::getPublishedTime).orderByDesc(Article::getUpdateTime);

        Page<Article> articlePage = this.page(new Page<>(request.getCurrent(), request.getPageSize()), wrapper);
        List<ArticleVo> articleVos = getArticleVos(articlePage.getRecords());
        Page<ArticleVo> articleVoPage = new Page<>(request.getCurrent(), request.getPageSize(), articlePage.getTotal());
        articleVoPage.setRecords(articleVos);
        return articleVoPage;
    }

    private Page<ArticleVo> emptyArticleVoPage(long current, long size) {
        Page<ArticleVo> page = new Page<>(current, size, 0);
        page.setRecords(Collections.emptyList());
        return page;
    }

    @Override
    public UploadBatchResponse batchUpload(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请上传文件");
        }
        if (files.length > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "单批次最多上传 5 个文件");
        }

        String batchId = UUID.randomUUID().toString().replace("-", "");
        List<Long> ids = new ArrayList<>(files.length);
        List<UploadBatchFileVo> fileVos = new ArrayList<>(files.length);

        for (int i = 0; i < files.length; i++) {
            Article placeholder = new Article();
            placeholder.setUploadStatus(UploadStatusEnum.UPLOADING.getCode());
            placeholder.setStatus(ArticleStatusEnum.DRAFT.getCode());
            this.save(placeholder);
            Long articleId = placeholder.getId();
            ids.add(articleId);

            String originalName = resolveFileName(files[i], i);
            UploadBatchFileVo fileVo = UploadBatchFileVo.builder()
                    .articleId(articleId)
                    .order(i)
                    .fileName(originalName)
                    .build();
            fileVos.add(fileVo);
        }

        uploadBatchManager.registerBatch(batchId, fileVos);
        fileVos.forEach(vo -> uploadBatchManager.emitFileQueued(batchId, vo));
        uploadBatchManager.emitBatchStatus(batchId, "running", "批次已创建，开始处理");

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
            fileNames[i] = resolveFileName(files[i], i);
        }

        CompletableFuture.runAsync(() -> {
            try {
                self.batchUploadAndSaveArticles(batchId, byteList, ids, fileNames);
            } catch (Exception e) {
                log.error("批量上传异步任务执行失败 batchId={}", batchId, e);
                uploadBatchManager.markBatchFailed(batchId, "批量上传异步任务异常");
            }
        }, articleGeneratorExecutor);

        return UploadBatchResponse.builder()
                .batchId(batchId)
                .files(fileVos)
                .build();
    }

    @Override
    public void batchUploadAndSaveArticles(String batchId, byte[][] byteList, List<Long> ids, String[] fileNames) {
        log.info("批量上传文章开始，batchId={}，文件数={}", batchId, byteList == null ? 0 : byteList.length);
        if (byteList == null || byteList.length == 0 || ids == null || ids.isEmpty()) {
            uploadBatchManager.markBatchFailed(batchId, "没有可处理的文件");
            return;
        }
        int total = Math.min(byteList.length, ids.size());
        for (int i = 0; i < total; i++) {
            byte[] bytes = byteList[i];
            Long articleId = ids.get(i);
            String originalName = (fileNames != null && fileNames.length > i && StringUtils.isNotBlank(fileNames[i]))
                    ? fileNames[i] : ("upload-" + i + ".txt");
            uploadBatchManager.emitFileStatus(batchId, buildPayload(batchId, articleId, originalName, i, "uploading", "文件已上传，准备解析"));
            try {
                String articleContent = this.bytesToString(bytes);
                uploadBatchManager.emitFileStatus(batchId, buildPayload(batchId, articleId, originalName, i, "processing", "AI 正在生成文章元数据"));

                Article toGen = new Article();
                toGen.setContent(articleContent);
                Article generated = chatService.generateArticleMetaData(toGen);

                String genTitle2 = generated.getTitle();
                if (StringUtils.isNotBlank(genTitle2) && this.lambdaQuery().eq(Article::getTitle, genTitle2).count() > 0) {
                    Article dupUpdate = new Article();
                    dupUpdate.setId(articleId);
                    dupUpdate.setContent(articleContent);
                    dupUpdate.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                    this.updateById(dupUpdate);
                    uploadBatchManager.emitFileStatus(batchId, buildPayload(batchId, articleId, originalName, i, "failed", "检测到重复标题"));
                    log.warn("批量上传：检测到重复标题，已拒绝上传并标记失败，title={}", genTitle2);
                    continue;
                }

                int readTime = (articleContent != null && articleContent.length() >= 100)
                        ? articleContent.length() / 100 : 1;

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

                ragService.storeFileToDashScopeCloudStore(bytes, originalName);
                uploadBatchManager.emitFileStatus(batchId, buildPayload(batchId, articleId, originalName, i, "success", "生成并入库成功"));
            } catch (Exception ex) {
                log.error("单篇文章上传处理失败，articleId={}。将标记为失败", articleId, ex);
                Article failUpdate = new Article();
                failUpdate.setId(articleId);
                failUpdate.setUploadStatus(UploadStatusEnum.FAILED.getCode());
                this.updateById(failUpdate);
                uploadBatchManager.emitFileStatus(batchId, buildPayload(batchId, articleId, originalName, i, "failed", ex.getMessage()));
            }
        }
        uploadBatchManager.markBatchCompleted(batchId);
        log.info("批量上传文章结束，batchId={}", batchId);
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

    private UploadProgressPayload buildPayload(String batchId, Long articleId, String fileName, int order, String status, String message) {
        return UploadProgressPayload.builder()
                .batchId(batchId)
                .articleId(articleId)
                .fileName(fileName)
                .order(order)
                .status(status)
                .message(message)
                .build();
    }

    private String resolveFileName(MultipartFile file, int index) {
        if (file == null) {
            return "upload-" + index + ".txt";
        }
        String name = file.getOriginalFilename();
        if (StringUtils.isBlank(name)) {
            return "upload-" + index + ".txt";
        }
        return name;
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

    @Override
    public List<ArticleVo> recommendArticles(Long articleId, int limit) {
        if (limit <= 0) {
            limit = 5;
        }
        if (articleId == null) {
            return buildArticleVos(randomRecommendIds(limit, null, Collections.emptySet()));
        }

        Article sourceArticle = this.getById(articleId);
        if (sourceArticle == null) {
            return Collections.emptyList();
        }

        List<Long> tagIds = articleTagService.lambdaQuery()
                .eq(ArticleTag::getArticleId, articleId)
                .list()
                .stream()
                .map(ArticleTag::getTagId)
                .toList();
        List<Long> columnIds = articleColumnService.lambdaQuery()
                .eq(ArticleColumn::getArticleId, articleId)
                .list()
                .stream()
                .map(ArticleColumn::getColumnId)
                .toList();

        Map<Long, Long> tagSimilarityMap = CollectionUtils.isEmpty(tagIds)
                ? Collections.emptyMap()
                : articleTagService.lambdaQuery()
                .in(ArticleTag::getTagId, tagIds)
                .list()
                .stream()
                .filter(record -> !articleId.equals(record.getArticleId()))
                .collect(Collectors.groupingBy(ArticleTag::getArticleId, Collectors.counting()));

        Set<Long> columnArticleIds = CollectionUtils.isEmpty(columnIds)
                ? Collections.emptySet()
                : articleColumnService.lambdaQuery()
                .in(ArticleColumn::getColumnId, columnIds)
                .ne(ArticleColumn::getArticleId, articleId)
                .list()
                .stream()
                .map(ArticleColumn::getArticleId)
                .collect(Collectors.toSet());

        List<Long> orderedIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(columnArticleIds) && !tagSimilarityMap.isEmpty()) {
            orderedIds.addAll(columnArticleIds.stream()
                    .filter(tagSimilarityMap::containsKey)
                    .sorted((a, b) -> Long.compare(tagSimilarityMap.get(b), tagSimilarityMap.get(a)))
                    .limit(limit)
                    .toList());
        }

        if (orderedIds.size() < limit && !tagSimilarityMap.isEmpty()) {
            orderedIds.addAll(tagSimilarityMap.entrySet().stream()
                    .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                    .map(Map.Entry::getKey)
                    .filter(id -> !id.equals(articleId) && !orderedIds.contains(id))
                    .limit(limit - orderedIds.size())
                    .toList());
        }

        if (orderedIds.size() < limit) {
            Set<Long> exclude = new HashSet<>(orderedIds);
            exclude.add(articleId);
            orderedIds.addAll(randomRecommendIds(limit - orderedIds.size(), articleId, exclude));
        }

        List<Long> finalIds = orderedIds.stream()
                .distinct()
                .limit(limit)
                .toList();
        return buildArticleVos(finalIds);
    }

    private List<ArticleVo> buildArticleVos(List<Long> orderedIds) {
        if (CollectionUtils.isEmpty(orderedIds)) {
            return Collections.emptyList();
        }
        List<Article> articles = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode())
                .in(Article::getId, orderedIds)
                .list();
        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, article -> article));
        List<ArticleVo> vos = new ArrayList<>();
        for (Long id : orderedIds) {
            Article article = articleMap.get(id);
            if (article != null) {
                vos.add(getArticleVoById(id));
            }
        }
        return vos;
    }

    private List<Long> randomRecommendIds(int limit, Long articleId, Collection<Long> excludeIds) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        Set<Long> excludeSet = new HashSet<>();
        if (articleId != null) {
            excludeSet.add(articleId);
        }
        if (CollectionUtils.isNotEmpty(excludeIds)) {
            excludeSet.addAll(excludeIds);
        }
        var query = this.lambdaQuery()
                .eq(Article::getStatus, ArticleStatusEnum.PUBLISHED.getCode());
        if (CollectionUtils.isNotEmpty(excludeSet)) {
            query.notIn(Article::getId, excludeSet);
        }
        List<Article> randomArticles = query.last("ORDER BY RANDOM() LIMIT " + limit).list();
        return randomArticles.stream().map(Article::getId).toList();
    }
}
