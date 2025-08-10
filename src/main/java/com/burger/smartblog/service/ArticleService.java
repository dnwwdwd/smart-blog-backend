package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.dto.article.ArticlePublishRequest;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.vo.ArticleVo;
import jakarta.validation.Valid;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【article】的数据库操作Service
 * @createDate 2025-08-10 11:45:32
 */
public interface ArticleService extends IService<Article> {

    void publishArticle(@Valid ArticlePublishRequest articlePublishRequest);

    Page<ArticleVo> getArticlePage(ArticleRequest request);

    List<Article> getArticlesByTagId(Long tagId);

    List<Article> getArticlesByColumnId(Long columnId);
}
