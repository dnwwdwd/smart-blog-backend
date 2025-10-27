package com.burger.smartblog.model.vo.upload;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 内部使用的 SSE 消息包装
 */
@Data
@AllArgsConstructor
public class UploadSseMessage {
    private String event;
    private Object data;
}
