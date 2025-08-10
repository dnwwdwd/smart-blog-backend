package com.burger.smartblog.service;

import com.burger.smartblog.model.entity.Column;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.vo.ColumnVo;

/**
* @author hejiajun
* @description 针对表【column】的数据库操作Service
* @createDate 2025-08-10 11:45:33
*/
public interface ColumnService extends IService<Column> {

    ColumnVo getColumnVoById(Long columnId);
}
