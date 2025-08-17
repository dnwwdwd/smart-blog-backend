package com.burger.smartblog.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String userAccount;

    @NotBlank(message = "密码不能为空")
    private String userPassword;

    @NotBlank(message = "确认密码不能为空")
    private String checkPassword;

}
