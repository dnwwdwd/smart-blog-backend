package com.burger.smartblog.enums;

/**
 * 友链状态枚举
 */
public enum FriendLinkEnum {
    NORMAL(1, "正常"),
    DISABLED(0, "禁用"),
    DELETED(-1, "已删除");

    private final int code;
    private final String desc;

    FriendLinkEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据 code 获取枚举
     */
    public static FriendLinkEnum fromCode(int code) {
        for (FriendLinkEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的友链状态码: " + code);
    }
}
