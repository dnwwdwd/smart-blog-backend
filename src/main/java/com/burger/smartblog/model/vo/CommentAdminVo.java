package com.burger.smartblog.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论后台管理展示对象
 */
@Data
public class CommentAdminVo implements Serializable {

    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long parentId;
    private String nickname;
    private String userEmail;
    private String userWebsite;
    private String userAvatar;
    private String content;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createTime;
    private Long userId;
}