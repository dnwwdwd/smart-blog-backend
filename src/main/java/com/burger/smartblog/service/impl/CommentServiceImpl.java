package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.mapper.CommentMapper;
import com.burger.smartblog.model.dto.comment.CommentDto;
import com.burger.smartblog.model.entity.Comment;
import com.burger.smartblog.model.vo.CommentVo;
import com.burger.smartblog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author hejiajun
 * @description 针对表【comment】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {

    @Override
    public void createComment(CommentDto dto, HttpServletRequest request) {
        Long articleId = dto.getArticleId();
        String nickname = dto.getNickname();
        String content = dto.getContent();
        String avatar = dto.getAvatar();
        String email = dto.getEmail();
        String website = dto.getWebsite();
        Long parentId = dto.getParentId();

        if (parentId != null && parentId > 0) {
            Long count = this.lambdaQuery()
                    .eq(Comment::getId, parentId)
                    .eq(Comment::getArticleId, articleId)
                    .count();
            if (count == 0) {
                throw new IllegalArgumentException("回复的上级评论不存在");
            }
        }

        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(x -> x.split(",")[0].trim())
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(request.getHeader("X-Real-IP")).orElse(request.getRemoteAddr()));
        String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");

        LocalDateTime now = LocalDateTime.now();
        Comment entity = new Comment();
        entity.setArticleId(dto.getArticleId());
        entity.setParentId(parentId == null ? 0L : parentId);
        entity.setNickname(nickname);
        entity.setUserEmail(email);
        entity.setUserWebsite(website);
        entity.setUserAvatar(avatar);
        entity.setContent(content);
        entity.setIpAddress(ip);
        entity.setUserAgent(ua);
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        this.save(entity);
    }


    @Override
    public List<CommentVo> getCommentsByArticleId(Long articleId) {
        if (articleId == null) {
            return new ArrayList<>();
        }

        List<Comment> allComments = this.lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .orderByAsc(Comment::getCreateTime)
                .list();

        if (allComments.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, CommentVo> voMap = new HashMap<>();
        for (Comment entity : allComments) {
            CommentVo vo = new CommentVo();
            vo.setId(entity.getId());
            vo.setAuthor(entity.getNickname());
            vo.setEmail(entity.getUserEmail());
            vo.setWebsite(entity.getUserWebsite());
            vo.setContent(entity.getContent());
            vo.setAvatar(entity.getUserAvatar());
            vo.setCreateTime(entity.getCreateTime()); // 👈 新增字段，方便排序
            vo.setReplies(new ArrayList<>());
            voMap.put(entity.getId(), vo);
        }

        List<CommentVo> roots = new ArrayList<>();
        for (Comment entity : allComments) {
            CommentVo current = voMap.get(entity.getId());
            if (entity.getParentId() == null || entity.getParentId() == 0L) {
                roots.add(current);
            } else {
                CommentVo parent = voMap.get(entity.getParentId());
                if (parent != null) {
                    parent.getReplies().add(current);
                } else {
                    roots.add(current);
                }
            }
        }

        // 递归排序
        sortRepliesByTime(roots);

        return roots;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        this.removeById(id);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, id);
        this.remove(wrapper);
    }

    private void sortRepliesByTime(List<CommentVo> list) {
        list.sort(Comparator.comparing(CommentVo::getCreateTime).reversed());
        for (CommentVo vo : list) {
            if (vo.getReplies() != null && !vo.getReplies().isEmpty()) {
                sortRepliesByTime(vo.getReplies());
            }
        }
    }

}




