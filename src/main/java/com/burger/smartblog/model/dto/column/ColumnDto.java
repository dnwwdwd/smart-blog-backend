package com.burger.smartblog.model.dto.column;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@Data
public class ColumnDto implements Serializable {

    private Long id;

    @NotBlank(message = "专栏名称不能为空")
    private String name;

    @NotBlank(message = "专栏描述不能为空")
    private String description;

    @NotBlank(message = "专栏封面不能为空")
    private String coverImage;

}
