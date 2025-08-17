package com.burger.smartblog.enums;

/**
 * /**
 * 文章状态枚举 (对应数据库字段: status VARCHAR(20) CHECK IN ('draft', 'published', 'archived'))
 */
public enum ArticleStatusEnum {
    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    ARCHIVED(2, "已归档");

    private final Integer code; // 数据库存储的值
    private final String desc; // 状态描述

    ArticleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据数据库 code 获取枚举
     */
    public static ArticleStatusEnum fromCode(Integer code) {
        for (ArticleStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的文章状态: " + code);
    }
}
