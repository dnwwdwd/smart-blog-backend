package com.burger.smartblog.model.dto.reward;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RewardMessageCreateRequest implements Serializable {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入正确的邮箱地址")
    private String email;

    @Size(max = 200, message = "网站链接过长")
    private String website;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 200, message = "留言内容不能超过200个字")
    private String message;

    @NotNull(message = "请输入打赏金额")
    @Positive(message = "打赏金额需大于0")
    private BigDecimal amount;
}
