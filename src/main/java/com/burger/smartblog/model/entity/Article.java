package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName article
 */
@TableName(value = "article")
@Data
@ToString
public class Article implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     *
     */
    private String title;

    /**
     *
     */
    private String content;

    /**
     *
     */
    private String excerpt;

    /**
     *
     */
    private String coverImage;

    /**
     *
     */
    private Integer status;

    /**
     *
     */
    private Integer readTime;

    /**
     *
     */
    private Integer views;

    /**
     *
     */
    private String seoTitle;

    /**
     *
     */
    private String seoDescription;

    /**
     *
     */
    private String seoKeywords;

    /**
     *
     */
    private Date publishedTime;

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