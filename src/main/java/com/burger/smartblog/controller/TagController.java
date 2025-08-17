package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.tag.TagDto;
import com.burger.smartblog.model.dto.tag.TagRequest;
import com.burger.smartblog.model.vo.TagVo;
import com.burger.smartblog.service.TagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/page")
    public BaseResponse<Page<TagVo>> getTagPage(@RequestBody TagRequest request) {
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

    @PostMapping("/update")
    public BaseResponse<Void> updateTag(@RequestBody TagDto dto) {
        tagService.update(dto);
        return ResultUtils.success();
    }

    @PostMapping("/add")
    public BaseResponse<Void> addTag(@RequestBody TagDto dto) {
        tagService.addTag(dto);
        return ResultUtils.success();
    }

    @PostMapping("/delete/{tagId}")
    public BaseResponse<Void> deleteTag(@PathVariable @Valid @NotNull Long tagId) {
        tagService.delete(tagId);
        return ResultUtils.success();
    }

}
