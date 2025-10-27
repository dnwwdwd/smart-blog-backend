package com.burger.smartblog.model.vo.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 中单个文件的实时状态载荷
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadProgressPayload {
    private String batchId;
    private Long articleId;
    private Integer order;
    private String fileName;
    /**
     * queued/uploading/processing/success/failed
     */
    private String status;
    private String message;
}
