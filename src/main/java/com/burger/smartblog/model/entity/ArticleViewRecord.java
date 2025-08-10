package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName article_view_record
 */
@TableName(value ="article_view_record")
@Data
public class ArticleViewRecord implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private Long articleId;

    /**
     * 
     */
    private Long userId;

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
    private Date viewedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}