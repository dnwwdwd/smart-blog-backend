package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.column.ColumnRequest;
import com.burger.smartblog.model.vo.ColumnVo;
import com.burger.smartblog.service.ColumnService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/page")
    public BaseResponse<Page<ColumnVo>> getColumnPage(@RequestBody ColumnRequest request) {
        return ResultUtils.success(columnService.getColumnPage(request));
    }

    /**
     * 根据 id 获取专栏详情
     *
     * @param columnId
     * @return
     */
    @GetMapping("/get/{columnId}")
    public BaseResponse<ColumnVo> getColumnById(@PathVariable Long columnId) {
        return ResultUtils.success(columnService.getColumnVoById(columnId));
    }

}
