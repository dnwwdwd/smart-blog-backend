package com.burger.smartblog.model.vo.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 中批次级别状态载荷
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBatchStatusPayload {
    /**
     * 批次 ID
     */
    private String batchId;

    /**
     * running/completed/failed
     */
    private String status;

    /**
     * 附加说明
     */
    private String message;
}
