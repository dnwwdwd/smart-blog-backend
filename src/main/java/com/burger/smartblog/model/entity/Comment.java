package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName comment
 */
@TableName(value ="comment")
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
    private Object ipAddress;

    /**
     * 
     */
    private String userAgent;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}