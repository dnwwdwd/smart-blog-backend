package com.burger.smartblog.enums;

/**
 * 打赏留言审核状态
 */
public enum RewardMessageStatusEnum {
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    RewardMessageStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static RewardMessageStatusEnum fromCode(Integer code) {
        if (code == null) {
            return PENDING;
        }
        for (RewardMessageStatusEnum statusEnum : values()) {
            if (statusEnum.code == code) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("不支持的审核状态: " + code);
    }
}
