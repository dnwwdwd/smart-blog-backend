package com.burger.smartblog.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.burger.smartblog.model.entity.Comment;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ArticleVo implements Serializable {

    private Long id;

    private String title;

    private String content;

    private String excerpt;

    private String coverImage;

    private Integer status;

    private Integer readTime;

    private Integer views;

    private String seoTitle;

    private String seoDescription;

    private List<String> seoKeywords;

    private List<String> tags;

    private List<Comment> comments;

    private Date publishedTime;

    private Date createTime;

    private Date updateTime;

}
