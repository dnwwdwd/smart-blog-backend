package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.tag.TagRequest;
import com.burger.smartblog.model.entity.Tag;
import com.burger.smartblog.model.vo.TagVo;
import com.burger.smartblog.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tag")
@AllArgsConstructor
public class TagController {

    private TagService tagService;

    /**
     * 获取标签列表
     *
     * @return
     */
    @GetMapping("/page")
    public BaseResponse<Page<TagVo>> getTagPage(TagRequest request) {
        Page<TagVo> tags = tagService.getTagPage(request);
        return ResultUtils.success(tags);
    }

    /**
     * 根据 id 获取标签详情
     *
     * @param tagId
     * @return
     */
    @GetMapping("/get/vo/{tagId}")
    public BaseResponse<TagVo> getTag(@PathVariable Long tagId) {
        TagVo tagVo = tagService.getTagVoById(tagId);
        return ResultUtils.success(tagVo);
    }

}
