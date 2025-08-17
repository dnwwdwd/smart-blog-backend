package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.model.dto.column.ColumnDto;
import com.burger.smartblog.model.dto.column.ColumnRequest;
import com.burger.smartblog.model.entity.Column;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.vo.ColumnVo;
import jakarta.validation.Valid;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【column】的数据库操作Service
 * @createDate 2025-08-10 11:45:33
 */
public interface ColumnService extends IService<Column> {

    ColumnVo getColumnVoById(Long columnId);

    List<Column> getColumnsByArticleId(Long id);

    Page<ColumnVo> getColumnPage(ColumnRequest request);

    void deleteColumn(Long columnId);

    void update(ColumnDto dto);

    void add(ColumnDto dto);
}
