package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.dto.article.ArticleDto;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.vo.ArticleVo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author hejiajun
 * @description 针对表【article】的数据库操作Service
 * @createDate 2025-08-10 11:45:32
 */
public interface ArticleService extends IService<Article> {

    Long publishArticle(ArticleDto articleDto);

    void updateArticle(ArticleDto articleDto);

    Page<ArticleVo> getArticlePage(ArticleRequest request);

    List<Article> getArticlesByTagId(Long tagId);

    List<Article> getArticlesByColumnId(Long columnId);

    ArticleVo getArticleVoById(Long articleId);

    Page<ArticleVo> getArticlePageByColumnId(Long columnId, ArticleRequest request);

    Page<ArticleVo> getArticlePageByTagId(Long tagId, ArticleRequest request);

    Page<ArticleVo> getAllArticles(ArticleRequest request);

    // 返回每个文件对应的占位文章ID，便于前端轮询
    List<Long> batchUpload(MultipartFile[] files);

    // 兼容旧实现，内部可能不再被直接调用
    void batchUploadAndSaveArticles(byte[][] byteList, List<Long> ids, String[] fileNames);

    // 轮询查询上传状态
    Map<Long, Integer> getUploadStatuses(List<Long> ids);

    // 针对失败任务的重试
    void retryUpload(Long id);

    // 删除文章
    void deleteArticle(Long articleId);

    // 推荐文章
    List<ArticleVo> recommendArticles(Long articleId, int limit);
}
