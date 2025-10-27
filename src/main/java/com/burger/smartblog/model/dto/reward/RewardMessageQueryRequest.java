package com.burger.smartblog.model.dto.reward;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class RewardMessageQueryRequest extends PageRequest implements Serializable {

    /**
     * 审核状态：0 待审核、1 通过、2 拒绝
     */
    private Integer status;

    /**
     * 按昵称关键字筛选
     */
    private String keyword;
}
