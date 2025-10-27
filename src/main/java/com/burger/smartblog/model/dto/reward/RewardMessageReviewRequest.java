package com.burger.smartblog.model.dto.reward;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class RewardMessageReviewRequest implements Serializable {

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Size(max = 200, message = "审核备注不能超过200个字符")
    private String reviewRemark;
}
