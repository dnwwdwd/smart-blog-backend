package com.burger.smartblog.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.model.dto.comment.CommentSubmitDto;
import com.burger.smartblog.model.entity.Comment;
import com.burger.smartblog.service.CommentService;
import com.burger.smartblog.mapper.CommentMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author hejiajun
 * @description 针对表【comment】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Override
    public void submitComment(CommentSubmitDto dto, HttpServletRequest request) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        // todo 随机设置用户头像
        comment.setUserAvatar("https://hejiajun-img-bucket.oss-cn-wuhan-lr.aliyuncs.com/img/image-20241119111441579.png");
        String ip = request.getRemoteAddr();
        comment.setIpAddress(ip);
        String userAgent = request.getHeader("User-Agent");
        comment.setUserAgent(userAgent);
        this.save(comment);
    }

    @Override
    public List<Comment> getCommentsByArticleId(Long articleId) {
        if (articleId == null) {
            return new ArrayList<>();
        }
        return this.lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime)
                .list();
    }

}




