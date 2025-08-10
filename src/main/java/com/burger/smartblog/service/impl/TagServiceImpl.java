package com.burger.smartblog.service.impl;

import cn.hutool.extra.cglib.CglibUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.model.entity.Article;
import com.burger.smartblog.model.entity.ArticleTag;
import com.burger.smartblog.model.entity.Tag;
import com.burger.smartblog.model.vo.TagVo;
import com.burger.smartblog.service.ArticleService;
import com.burger.smartblog.service.ArticleTagService;
import com.burger.smartblog.service.TagService;
import com.burger.smartblog.mapper.TagMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【tag】的数据库操作Service实现
 * @createDate 2025-08-10 11:45:33
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
        implements TagService {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleTagService articleTagService;

    @Override
    public List<Tag> getTagsByArticleId(Long articleId) {
        if (articleId == null) {
            return new ArrayList<>();
        }
        List<Long> tagIds = articleTagService.lambdaQuery()
                .eq(ArticleTag::getArticleId, articleId)
                .list().stream().map(ArticleTag::getTagId).toList();
        return this.listByIds(tagIds);
    }

    @Override
    public TagVo getTagVoById(Long tagId) {
        if (tagId == null) {
            return null;
        }
        Tag tag = this.getById(tagId);
        return this.getTagVo(tag);
    }

    public TagVo getTagVo(Tag tag) {
        if (tag == null) {
            return null;
        }
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag, tagVo);
        List<Article> articles = articleService.getArticlesByTagId(tag.getId());
        tagVo.setArticles(articles);
        tagVo.setArticleCount(articles.size());
        return tagVo;
    }

}




