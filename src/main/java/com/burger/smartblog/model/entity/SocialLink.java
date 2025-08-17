package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 社交链接表
 * @TableName social_link
 */
@TableName(value ="social_link")
@Data
public class SocialLink implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private Integer friendLinkId;

    /**
     * 图标类型: qq, wechat, heart, star等
     */
    private String iconType;

    /**
     * 社交链接URL
     */
    private String iconUrl;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 
     */
    private Date createdTime;

    /**
     * 
     */
    private Date updatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}