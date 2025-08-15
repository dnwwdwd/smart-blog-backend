package com.burger.smartblog.model.dto.tag;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

/**
 *@Author: hejiajun
 *@CreateTime: 2025-08-11
 *@Description:
 */
@Data
public class TagRequest extends PageRequest {

    private String tagName;

}


