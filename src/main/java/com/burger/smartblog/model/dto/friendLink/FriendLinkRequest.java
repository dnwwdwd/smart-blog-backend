package com.burger.smartblog.model.dto.friendLink;

import com.baomidou.mybatisplus.annotation.TableId;
import com.burger.smartblog.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class FriendLinkRequest extends PageRequest implements Serializable {

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

    private String searchKeyword;

    private static final long serialVersionUID = 1L;

}
