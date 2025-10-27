package com.burger.smartblog.model.dto.article;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

@Data
public class ArticleRequest extends PageRequest {

    /**
     * 模糊搜索关键词（标题或摘要）
     */
    private String keyword;

    /**
     * 兼容旧字段
     */
    private String title;

    /**
     * 文章状态
     */
    private Integer status;

    /**
     * 所属专栏
     */
    private Long columnId;

    /**
     * 标签
     */
    private Long tagId;

    /**
     * 发布时间范围（毫秒时间戳）
     */
    private Long publishStartTime;

    private Long publishEndTime;

}
