package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @TableName column
 */
@TableName("\"column\"")
@Data
public class Column implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     *
     */
    private String name;

    /**
     *
     */
    private String description;

    /**
     *
     */
    private String coverImage;

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