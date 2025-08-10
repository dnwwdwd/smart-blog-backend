package com.burger.smartblog.controller;

import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.entity.Column;
import com.burger.smartblog.model.vo.ColumnVo;
import com.burger.smartblog.service.ColumnService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/column")
@AllArgsConstructor
public class ColumnController {

    private ColumnService columnService;

    /**
     * 获取专栏列表
     *
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<Column>> list() {
        return ResultUtils.success(columnService.list());
    }

    /**
     * 根据 id 获取专栏详情
     *
     * @param columnId
     * @return
     */
    @GetMapping("/get/{columnId}")
    public BaseResponse<ColumnVo> get(@PathVariable Long columnId) {
        return ResultUtils.success(columnService.getColumnVoById(columnId));
    }

}
