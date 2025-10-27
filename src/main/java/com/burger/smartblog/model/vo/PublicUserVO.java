package com.burger.smartblog.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 对外公开的作者信息
 */
@Data
public class PublicUserVO implements Serializable {

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

    private static final long serialVersionUID = 1L;
}
