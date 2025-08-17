package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.model.dto.friendLink.FriendLinkDto;
import com.burger.smartblog.model.dto.friendLink.FriendLinkRequest;
import com.burger.smartblog.model.entity.FriendLink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.vo.FriendLinkVo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
* @author hejiajun
* @description 针对表【friend_link(友情链接主表)】的数据库操作Service
* @createDate 2025-08-16 10:20:46
*/
public interface FriendLinkService extends IService<FriendLink> {

    void addFriendLink(@Valid FriendLinkDto dto);

    Page<FriendLinkVo> getFriendLinkPage(FriendLinkRequest request);

    void deleteFriendLink(@Valid @NotNull Long friendLinkId);

    void updateFriendLink(@Valid FriendLinkDto dto);
}
