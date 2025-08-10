package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @TableName site_setting
 */
@TableName(value = "site_setting")
@Data
public class SiteSetting implements Serializable {
    /**
     *
     */
    @TableId
    private Integer id;

    /**
     *
     */
    private String settingKey;

    /**
     *
     */
    private String settingValue;

    /**
     *
     */
    private String settingType;

    /**
     *
     */
    private String description;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}