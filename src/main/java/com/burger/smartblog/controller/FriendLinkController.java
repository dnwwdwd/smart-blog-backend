package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.friendLink.FriendLinkDto;
import com.burger.smartblog.model.dto.friendLink.FriendLinkRequest;
import com.burger.smartblog.model.vo.FriendLinkVo;
import com.burger.smartblog.service.FriendLinkService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend/link")
@Validated
public class FriendLinkController {

    @Resource
    private FriendLinkService friendLinkService;

    @PostMapping("/add")
    public BaseResponse<Void> addFriendLink(@RequestBody @Valid FriendLinkDto dto) {
        friendLinkService.addFriendLink(dto);
        return ResultUtils.success();
    }

    @PostMapping("/page")
    public BaseResponse<Page<FriendLinkVo>> getFriendLinkPage(@RequestBody FriendLinkRequest request) {
        return ResultUtils.success(friendLinkService.getFriendLinkPage(request));
    }

    @PostMapping("/delete/{friendLinkId}")
    public BaseResponse<Void> deleteFriendLink(@PathVariable @Valid @NotNull Long friendLinkId) {
        friendLinkService.deleteFriendLink(friendLinkId);
        return ResultUtils.success();
    }

    @PostMapping("/update")
    public BaseResponse<Void> updateFriendLink(@RequestBody @Valid FriendLinkDto dto) {
        friendLinkService.updateFriendLink(dto);
        return ResultUtils.success();
    }




}
