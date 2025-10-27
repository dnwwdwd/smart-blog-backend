package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 打赏留言实体
 */
@TableName("reward_message")
@Data
public class RewardMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支持者昵称
     */
    private String nickname;

    /**
     * 联系邮箱
     */
    private String email;

    /**
     * 个人或项目网站
     */
    private String website;

    /**
     * 留言内容
     */
    private String message;

    /**
     * 打赏金额
     */
    private BigDecimal amount;

    /**
     * 审核状态：0 待审核、1 通过、2 拒绝
     */
    private Integer status;

    /**
     * 审核备注
     */
    private String reviewRemark;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除标记
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
