package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.enums.FriendLinkEnum;
import com.burger.smartblog.model.dto.friendLink.FriendLinkDto;
import com.burger.smartblog.model.dto.friendLink.FriendLinkRequest;
import com.burger.smartblog.model.dto.soicalLink.SocialLinkAddDto;
import com.burger.smartblog.model.entity.FriendLink;
import com.burger.smartblog.model.entity.SocialLink;
import com.burger.smartblog.model.vo.FriendLinkVo;
import com.burger.smartblog.service.FriendLinkService;
import com.burger.smartblog.mapper.FriendLinkMapper;
import com.burger.smartblog.service.SocialLinkService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author hejiajun
 * @description 针对表【friend_link(友情链接主表)】的数据库操作Service实现
 * @createDate 2025-08-16 10:20:46
 */
@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkMapper, FriendLink>
        implements FriendLinkService {

    @Resource
    private SocialLinkService socialLinkService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFriendLink(FriendLinkDto dto) {
        FriendLink friendLink = new FriendLink();
        BeanUtils.copyProperties(dto, friendLink);
        this.save(friendLink);
        // 插入成功后，返回插入后的ID
        Integer friendLinkId = friendLink.getId();
        List<SocialLinkAddDto> socialLinkAddDtos = dto.getSocialLinks();
        List<SocialLink> socialLinks = socialLinkAddDtos.stream().map(socialLinkAddDto -> {
            SocialLink socialLink = new SocialLink();
            BeanUtils.copyProperties(socialLinkAddDto, socialLink);
            socialLink.setFriendLinkId(friendLinkId);
            return socialLink;
        }).toList();
        socialLinkService.saveBatch(socialLinks);
    }

    @Override
    public Page<FriendLinkVo> getFriendLinkPage(FriendLinkRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        Page<FriendLink> friendLinkPage = this
                .lambdaQuery()
                .like(StringUtils.isNotBlank(request.getName()), FriendLink::getName, request.getName())
                .eq(request.getStatus() != null, FriendLink::getStatus, request.getStatus())
                .orderByAsc(FriendLink::getSortOrder)
                .page(new Page<>(current, pageSize));

        List<FriendLinkVo> friendLinkVos = friendLinkPage.getRecords().stream().map(friendLink -> {
            FriendLinkVo friendLinkVo = new FriendLinkVo();
            BeanUtils.copyProperties(friendLink, friendLinkVo);
            List<SocialLink> socialLinks = socialLinkService.lambdaQuery()
                    .eq(SocialLink::getFriendLinkId, friendLink.getId())
                    .orderByAsc(SocialLink::getSortOrder)
                    .list();
            friendLinkVo.setSocialLinks(socialLinks);
            return friendLinkVo;
        }).toList();
        Page<FriendLinkVo> friendLinkVoPage = new Page<>(current, pageSize, friendLinkPage.getTotal());
        friendLinkVoPage.setRecords(friendLinkVos);
        return friendLinkVoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriendLink(Long friendLinkId) {
        this.removeById(friendLinkId);
        socialLinkService.remove(new LambdaQueryWrapper<SocialLink>()
                .eq(SocialLink::getFriendLinkId, friendLinkId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFriendLink(FriendLinkDto dto) {
        FriendLink friendLink = this.getById(dto.getId());
        BeanUtils.copyProperties(dto, friendLink);
        this.updateById(friendLink);
        List<SocialLinkAddDto> socialLinkAddDtos = dto.getSocialLinks();
        if (CollectionUtils.isNotEmpty(socialLinkAddDtos)) {
            List<SocialLink> socialLinks = socialLinkAddDtos.stream().map(socialLinkAddDto -> {
                SocialLink socialLink = new SocialLink();
                BeanUtils.copyProperties(socialLinkAddDto, socialLink);
                return socialLink;
            }).toList();
            socialLinkService.updateBatchById(socialLinks);
        }
    }

}




