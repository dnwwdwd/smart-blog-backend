package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 友情链接主表
 * @TableName friend_link
 */
@TableName(value ="friend_link")
@Data
public class FriendLink implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 网站名称
     */
    private String name;

    /**
     * 网站描述
     */
    private String description;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 网站链接
     */
    private String url;

    /**
     * 是否为特殊卡片
     */
    private Boolean isSpecial;

    /**
     * 状态标签(如PREMIUM, VIP等)
     */
    private String statusLabel;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态: 1-正常, 0-禁用, -1-删除
     */
    private Integer status;

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