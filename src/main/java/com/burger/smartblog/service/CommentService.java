package com.burger.smartblog.service;

import com.burger.smartblog.model.dto.comment.CommentSubmitDto;
import com.burger.smartblog.model.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

/**
* @author hejiajun
* @description 针对表【comment】的数据库操作Service
* @createDate 2025-08-10 11:45:33
*/
public interface CommentService extends IService<Comment> {

    void submitComment(@Valid CommentSubmitDto dto, HttpServletRequest request);

    List<Comment> getCommentsByArticleId(Long articleId);
}
