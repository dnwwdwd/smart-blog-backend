package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.model.dto.comment.CommentDto;
import com.burger.smartblog.model.dto.comment.CommentRequest;
import com.burger.smartblog.model.entity.Comment;
import com.burger.smartblog.model.vo.CommentAdminVo;
import com.burger.smartblog.model.vo.CommentVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【comment】的数据库操作Service
 * @createDate 2025-08-10 11:45:33
 */
public interface CommentService extends IService<Comment> {

    void createComment(CommentDto dto, HttpServletRequest request);

    List<CommentVo> getCommentsByArticleId(Long articleId);

    void remove(Long id);

    Page<CommentAdminVo> getCommentPage(CommentRequest request);
}
