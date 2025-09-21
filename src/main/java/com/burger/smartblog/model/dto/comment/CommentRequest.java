package com.burger.smartblog.model.dto.comment;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

/**
 * 评论管理分页查询请求
 */
@Data
public class CommentRequest extends PageRequest {

    /**
     * 文章 ID（可选，按所属文章筛选）
     */
    private Long articleId;

    /**
     * 关键字（可选，按昵称 / 邮箱 / 内容模糊搜索）
     */
    private String searchKeyword;
}