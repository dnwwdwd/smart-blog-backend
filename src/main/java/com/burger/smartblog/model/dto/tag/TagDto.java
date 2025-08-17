package com.burger.smartblog.model.dto.tag;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class TagDto implements Serializable {

    private Long id;

    @NotBlank(message = "标签名称不能为空")
    private String name;

    @NotBlank(message = "标签描述不能为空")
    private String description;

    @NotBlank(message = "标签颜色不能为空")
    private String color;

    private static final long serialVersionUID = 1L;

}
