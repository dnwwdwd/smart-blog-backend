package com.burger.smartblog.model.vo;

import com.burger.smartblog.model.entity.Article;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class TagVo implements Serializable {

    private Long id;

    private String name;

    private String description;

    private String color;

    private Date createTime;

    private Date updateTime;

    private List<Article> articles;

    private Integer articleCount;

}
