package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.article.ArticlePublishRequest;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
@Validated
@AllArgsConstructor
public class ArticleController {

    private ArticleService articleService;

    /*
     * 发布文章
     */
    @PostMapping("/publish")
    public BaseResponse<Void> publishArticle(@RequestBody @Valid ArticlePublishRequest articlePublishRequest) {
        articleService.publishArticle(articlePublishRequest);
        return ResultUtils.success();
    }

    /**
     * 分页获取文章
     *
     * @param request
     * @return
     */
    @PostMapping("/page")
    public BaseResponse<Page<ArticleVo>> getArticlePage(@RequestBody ArticleRequest request) {
        Page<ArticleVo> page = articleService.getArticlePage(request);
        return ResultUtils.success(page);
    }

    @GetMapping("/get/vo/{articleId}")
    public BaseResponse<ArticleVo> getArticleVoById(@PathVariable Long articleId) {
        ArticleVo articleVo = articleService.getArticleVoById(articleId);
        return ResultUtils.success(articleVo);
    }

    @PostMapping("/column/get/vo/{columnId}")
    public BaseResponse<Page<ArticleVo>> getArticlePageByColumnId(@PathVariable Long columnId, @RequestBody ArticleRequest request) {
        return ResultUtils.success(articleService.getArticlePageByColumnId(columnId, request));
    }

    @PostMapping("/tag/get/vo/{tagId}")
    public BaseResponse<Page<ArticleVo>> getArticlePageByTagId(@PathVariable Long tagId, @RequestBody ArticleRequest request) {
        return ResultUtils.success(articleService.getArticlePageByTagId(tagId, request));
    }

    @PostMapping("/list/all")
    public BaseResponse<Page<ArticleVo>> getAllArticles(@RequestBody ArticleRequest request) {
        return ResultUtils.success(articleService.getAllArticles(request));
    }

}
