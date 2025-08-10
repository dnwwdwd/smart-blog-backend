package com.burger.smartblog.service.impl;

import cn.hutool.extra.cglib.CglibUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.entity.Column;
import com.burger.smartblog.model.vo.ColumnVo;
import com.burger.smartblog.service.ArticleService;
import com.burger.smartblog.service.ColumnService;
import com.burger.smartblog.mapper.ColumnMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【column】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class ColumnServiceImpl extends ServiceImpl<ColumnMapper, Column>
        implements ColumnService {

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




