package com.burger.smartblog.controller;

import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.comment.CommentSubmitDto;
import com.burger.smartblog.model.entity.Comment;
import com.burger.smartblog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *@Author: hejiajun
 *@CreateTime: 2025-08-11
 *@Description:
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
    public BaseResponse<Void> submitComment(@RequestBody @Valid CommentSubmitDto dto, HttpServletRequest request) {
        commentService.submitComment(dto, request);
        return ResultUtils.success();
    }

    @GetMapping("/get/{articleId}")
    public BaseResponse<List<Comment>> getComment(@PathVariable Long articleId) {
        List<Comment> comments = commentService.getCommentsByArticleId(articleId);
        return ResultUtils.success(comments);
    }

}


