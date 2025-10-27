package com.burger.smartblog.model.vo.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量上传接口返回的批次信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadBatchResponse {

    /**
     * 批次唯一编号
     */
    private String batchId;

    /**
     * 单批次中每个文件的占位信息
     */
    private List<UploadBatchFileVo> files;
}
