package com.burger.smartblog.controller;

import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.comment.CommentDto;
import com.burger.smartblog.model.vo.CommentVo;
import com.burger.smartblog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: hejiajun
 * @CreateTime: 2025-08-11
 * @Description:
 */
@RestController
@RequestMapping("/comment")
@Validated
@AllArgsConstructor
public class CommentController {

    private CommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping("/submit")
    public BaseResponse<Void> submitComment(@RequestBody @Valid CommentDto dto, HttpServletRequest request) {
        commentService.createComment(dto, request);
        return ResultUtils.success();
    }

    /**
     * 获取文章下的评论
     *
     * @param articleId
     * @return
     */
    @GetMapping("/get/{articleId}")
    public BaseResponse<List<CommentVo>> getComment(@PathVariable Long articleId) {
        List<CommentVo> comments = commentService.getCommentsByArticleId(articleId);
        return ResultUtils.success(comments);
    }

    @PostMapping("/delete/{id}")
    public BaseResponse<Void> deleteComment(@PathVariable @Valid @NotNull Long id) {
        commentService.remove(id);
        return ResultUtils.success();
    }

}


