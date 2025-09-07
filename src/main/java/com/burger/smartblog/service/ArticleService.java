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

    void batchUpload(MultipartFile[] files);

    void batchUploadAndSaveArticles(byte[][] byteList);
}
