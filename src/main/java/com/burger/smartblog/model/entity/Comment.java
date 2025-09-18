package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.burger.smartblog.handler.InetTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @TableName comment
 */
@TableName(value = "comment")
@Data
public class Comment implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     *
     */
    private Long articleId;

    /**
     *
     */
    private Long parentId;

    /**
     *
     */
    private String nickname;

    /**
     *
     */
    private String userEmail;

    /**
     *
     */
    private String userWebsite;

    /**
     *
     */
    private String userAvatar;

    /**
     *
     */
    private String content;

    /**
     *
     */
    @TableField(value = "ip_address", jdbcType = JdbcType.OTHER, typeHandler = InetTypeHandler.class)
    private String ipAddress;


    /**
     *
     */
    private String userAgent;

    /**
     *
     */
    private LocalDateTime createTime;

    /**
     *
     */
    private LocalDateTime updateTime;

    private Long userId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}