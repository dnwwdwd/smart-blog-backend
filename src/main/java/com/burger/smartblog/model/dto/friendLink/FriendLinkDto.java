package com.burger.smartblog.model.dto.friendLink;

import com.burger.smartblog.model.dto.soicalLink.SocialLinkAddDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FriendLinkDto implements Serializable {

    private Long id;

    /**
     * 网站名称
     */
    @NotBlank(message = "网站名称不能为空")
    private String name;

    /**
     * 网站描述
     */
    @NotBlank(message = "网站描述不能为空")
    private String description;

    /**
     * 头像URL
     */
    @NotBlank(message = "头像不能为空")
    private String avatar;

    /**
     * 网站链接
     */
    @NotBlank(message = "网站链接不能为空")
    private String url;

    /**
     * 是否为特殊链接
     */
    private Boolean isSpecial;

    @NotEmpty
    private List<SocialLinkAddDto> socialLinks;

    /**
     * 状态标签
     */
    private String statusLabel;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    private Integer status;

    private static final long serialVersionUID = 1L;

}
