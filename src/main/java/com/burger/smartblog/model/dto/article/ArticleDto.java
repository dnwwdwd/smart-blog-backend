package com.burger.smartblog.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleDto implements Serializable {

    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String excerpt;

    @NotBlank(message = "封面不能为空")
    private String coverImage;

    private String seoTitle;

    private String seoDescription;

    private List<String> seoKeywords;

    @NotEmpty(message = "标签不能为空")
    private List<String> tags;

    @NotEmpty(message = "专栏不能为空")
    private List<Long> columnIds;

    @NotNull(message = "状态不能为空")
    private Integer status;

}
