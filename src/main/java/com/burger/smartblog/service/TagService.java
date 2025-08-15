package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.dto.tag.TagRequest;
import com.burger.smartblog.model.entity.Tag;
import com.burger.smartblog.model.vo.TagVo;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【tag】的数据库操作Service
 * @createDate 2025-08-10 11:45:33
 */
public interface TagService extends IService<Tag> {

    List<Tag> getTagsByArticleId(Long articleId);

    TagVo getTagVoById(Long tagId);

    Page<TagVo> getTagPage(TagRequest request);
}
