package com.burger.smartblog.model.dto.column;

import com.burger.smartblog.common.PageRequest;
import lombok.Data;

/**
 *@Author: hejiajun
 *@CreateTime: 2025-08-11
 *@Description:
 */
@Data
public class ColumnRequest extends PageRequest {

    private String columnName;

}


