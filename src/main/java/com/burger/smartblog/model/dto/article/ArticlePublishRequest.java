package com.burger.smartblog.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticlePublishRequest implements Serializable {

    private String title;

    private String content;

    private String excerpt;

    private String coverImage;

    private String seoTitle;

    private String seoDescription;

    private String seoKeywords;

    private List<String> tags;

    private List<Long> columnIds;

}
