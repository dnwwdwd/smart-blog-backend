package com.burger.smartblog.model.vo.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端在单批次上传中对应的文件占位信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBatchFileVo {
    /**
     * 服务器端占位文章 ID
     */
    private Long articleId;

    /**
     * 前端提交的顺序编号（从 0 开始）
     */
    private Integer order;

    /**
     * 原始文件名
     */
    private String fileName;
}
