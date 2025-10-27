package com.burger.smartblog.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户更新操作结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否需要强制重新登录
     */
    private boolean needLogout;
}
