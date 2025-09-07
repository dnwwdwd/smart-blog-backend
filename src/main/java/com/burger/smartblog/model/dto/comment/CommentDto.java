package com.burger.smartblog.model.dto.comment;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDto {
    @NotNull(message = "文章 id 不能为空")
    private Long articleId;

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    @NotBlank(message = "邮件不能为空")
    private String email;

    @NotBlank(message = "请输入评论内容")
    private String content;

    private String website;

    private Long parentId;

    private String avatar;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}