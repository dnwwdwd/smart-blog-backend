package com.burger.smartblog.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户资料更新请求
 */
@Data
public class UserUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 登录账号
     */
    private String userAccount;

    /**
     * 当前密码（用于敏感操作校验）
     */
    private String currentPassword;

    /**
     * 新密码
     */
    private String newPassword;
}
