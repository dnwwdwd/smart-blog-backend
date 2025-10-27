package com.burger.smartblog.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class RewardMessageVo implements Serializable {

    private Long id;

    private String nickname;

    private String email;

    private String website;

    private String message;

    private BigDecimal amount;

    private Integer status;

    private String reviewRemark;

    private Date reviewTime;

    private Date createTime;
}
