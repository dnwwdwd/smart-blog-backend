package com.burger.smartblog.model.dto.soicalLink;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class SocialLinkAddDto implements Serializable {

    private Long id;

    private Integer friendLinkId;

    /**
     * 图标类型: qq, wechat, heart, star等
     */
    @NotBlank(message = "图标类型不能为空")
    private String iconType;

    /**
     * 社交链接URL
     */
    @NotBlank(message = "图标URL不能为空")
    private String iconUrl;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    private static final long serialVersionUID = 1L;

}
