package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.article.ArticleDto;
import com.burger.smartblog.model.dto.article.ArticleRequest;
import com.burger.smartblog.model.vo.ArticleVo;
import com.burger.smartblog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
    public BaseResponse<Long> publishArticle(@RequestBody @Valid ArticleDto articleDto) {
        return ResultUtils.success(articleService.publishArticle(articleDto));
    }

    /**
     * 更新文章
     */
    @PostMapping("/update")
    public BaseResponse<Void> updateArticle(@RequestBody @Valid ArticleDto articleDto) {
        articleService.updateArticle(articleDto);
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

    @PostMapping("/batch/upload")
    public BaseResponse<List<Long>> batchUpload(@RequestParam("files") MultipartFile[] files) {
        List<Long> ids = articleService.batchUpload(files);
        return ResultUtils.success(ids);
    }

    /**
     * 轮询获取上传处理状态
     * ids 通过多值参数传递，如 /article/upload/status?ids=1&ids=2
     */
    @GetMapping("/upload/status")
    public BaseResponse<Map<Long, Integer>> getUploadStatuses(@RequestParam("ids") List<Long> ids) {
        Map<Long, Integer> statusMap = articleService.getUploadStatuses(ids);
        return ResultUtils.success(statusMap);
    }

    /**
     * 重试处理失败的上传
     */
    @PostMapping("/upload/retry/{id}")
    public BaseResponse<Void> retryUpload(@PathVariable Long id) {
        articleService.retryUpload(id);
        return ResultUtils.success();
    }

    @PostMapping("/delete/{articleId}")
    public BaseResponse<Void> deleteArticle(@PathVariable Long articleId) {
        articleService.deleteArticle(articleId);
        return ResultUtils.success();
    }

    /**
     * 推荐文章
     */
    @GetMapping("/recommend/{articleId}")
    public BaseResponse<List<ArticleVo>> recommendArticles(@PathVariable Long articleId, @RequestParam(defaultValue = "5") int limit) {
        List<ArticleVo> articles = articleService.recommendArticles(articleId, limit);
        return ResultUtils.success(articles);
    }
}
