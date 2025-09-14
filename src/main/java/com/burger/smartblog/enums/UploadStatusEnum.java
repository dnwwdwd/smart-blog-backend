package com.burger.smartblog.enums;

import lombok.Getter;

/**
 * 文章批量上传的处理状态
 */
@Getter
public enum UploadStatusEnum {
    UPLOADING(0, "上传中"),
    SUCCESS(1, "成功"),
    FAILED(2, "失败");

    private final int code;
    private final String desc;

    UploadStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UploadStatusEnum fromCode(Integer code) {
        if (code == null) return null;
        for (UploadStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}