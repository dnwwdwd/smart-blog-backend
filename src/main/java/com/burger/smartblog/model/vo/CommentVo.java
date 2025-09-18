package com.burger.smartblog.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVo implements Serializable {

    private Long id;
    private String author;
    private String email;
    private String website;
    private String content;
    private String avatar;
    private List<CommentVo> replies;
    private LocalDateTime createTime;
    private Long userId;
}
