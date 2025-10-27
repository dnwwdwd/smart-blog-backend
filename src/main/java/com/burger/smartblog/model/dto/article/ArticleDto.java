package com.burger.smartblog.model.dto.article;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleDto implements Serializable {

    private Long id;

    private String title;

    private String content;

    private String excerpt;

    private String coverImage;

    private String seoTitle;

    private String seoDescription;

    private List<String> seoKeywords;

    private List<String> tags;

    private List<Long> columnIds;

    @NotNull(message = "状态不能为空")
    private Integer status;

}
