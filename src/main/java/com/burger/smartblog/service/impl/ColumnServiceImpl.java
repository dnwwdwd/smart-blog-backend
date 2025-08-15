package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.mapper.ColumnMapper;
import com.burger.smartblog.model.dto.column.ColumnRequest;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.entity.ArticleColumn;
import com.burger.smartblog.model.entity.Column;
import com.burger.smartblog.model.vo.ColumnVo;
import com.burger.smartblog.service.ArticleColumnService;
import com.burger.smartblog.service.ArticleService;
import com.burger.smartblog.service.ColumnService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author hejiajun
 * @description 针对表【column】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class ColumnServiceImpl extends ServiceImpl<ColumnMapper, Column>
        implements ColumnService {

    @Resource
    private ArticleColumnService articleColumnService;

    @Resource
    private ArticleService articleService;

    @Override
    public ColumnVo getColumnVoById(Long columnId) {
        if (columnId == null) {
            return null;
        }
        Column column = this.getById(columnId);
        return this.getColumnVo(column);
    }

    @Override
    public List<Column> getColumnsByArticleId(Long id) {
        if (id == null) {
            return new ArrayList<>();
        }
        List<Long> columnIds = articleColumnService.lambdaQuery()
                .eq(ArticleColumn::getArticleId, id)
                .list().stream().map(ArticleColumn::getColumnId).toList();
        return this.listByIds(columnIds);
    }

    @Override
    public Page<ColumnVo> getColumnPage(ColumnRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        String columnName = request.getColumnName();
        Page<Column> columnPage = this.lambdaQuery()
                .like(StringUtils.isNotBlank(columnName), Column::getName, columnName)
                .orderByDesc(Column::getCreateTime)
                .page(new Page<>(current, pageSize));
        List<ColumnVo> columnVos = columnPage.getRecords().stream().map(this::getColumnVo).toList();
        Page<ColumnVo> columnVoPage = new Page<>(current, pageSize, columnPage.getTotal());
        columnVoPage.setRecords(columnVos);
        return columnVoPage;
    }

    public ColumnVo getColumnVo(Column column) {
        if (column == null) {
            return null;
        }
        ColumnVo columnVo = new ColumnVo();
        BeanUtils.copyProperties(column, columnVo);
        List<Article> articles = articleService.getArticlesByColumnId(column.getId());
        columnVo.setArticles(articles);
        columnVo.setArticleCount(articles.size());
        return columnVo;
    }
}




