package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.common.ErrorCode;
import com.burger.smartblog.enums.RewardMessageStatusEnum;
import com.burger.smartblog.exception.BusinessException;
import com.burger.smartblog.mapper.RewardMessageMapper;
import com.burger.smartblog.model.dto.reward.RewardMessageCreateRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageQueryRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageReviewRequest;
import com.burger.smartblog.model.entity.RewardMessage;
import com.burger.smartblog.model.vo.RewardMessageVo;
import com.burger.smartblog.service.RewardMessageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RewardMessageServiceImpl extends ServiceImpl<RewardMessageMapper, RewardMessage>
        implements RewardMessageService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRewardMessage(RewardMessageCreateRequest request) {
        RewardMessage rewardMessage = new RewardMessage();
        BeanUtils.copyProperties(request, rewardMessage);
        rewardMessage.setStatus(RewardMessageStatusEnum.PENDING.getCode());
        rewardMessage.setCreateTime(new Date());
        rewardMessage.setUpdateTime(new Date());
        rewardMessage.setIsDelete(0);
        if (rewardMessage.getAmount() == null) {
            rewardMessage.setAmount(request.getAmount());
        }
        this.save(rewardMessage);
        return rewardMessage.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewRewardMessage(Long id, RewardMessageReviewRequest request) {
        RewardMessage rewardMessage = this.getById(id);
        if (rewardMessage == null || (rewardMessage.getIsDelete() != null && rewardMessage.getIsDelete() == 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "打赏留言不存在");
        }
        RewardMessageStatusEnum statusEnum = RewardMessageStatusEnum.fromCode(request.getStatus());
        rewardMessage.setStatus(statusEnum.getCode());
        rewardMessage.setReviewRemark(StringUtils.trimToNull(request.getReviewRemark()));
        rewardMessage.setReviewTime(new Date());
        rewardMessage.setUpdateTime(new Date());
        return this.updateById(rewardMessage);
    }

    @Override
    public Page<RewardMessage> getRewardMessagePage(RewardMessageQueryRequest request) {
        int current = Math.max(1, request.getCurrent());
        int pageSize = Math.max(1, request.getPageSize());
        LambdaQueryWrapper<RewardMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RewardMessage::getIsDelete, 0);
        if (request.getStatus() != null) {
            queryWrapper.eq(RewardMessage::getStatus, request.getStatus());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.like(RewardMessage::getNickname, request.getKeyword());
        }
        queryWrapper.orderByDesc(RewardMessage::getReviewTime)
                .orderByDesc(RewardMessage::getCreateTime);
        return this.page(new Page<>(current, pageSize), queryWrapper);
    }

    @Override
    public List<RewardMessageVo> listApprovedMessages(int limit) {
        LambdaQueryWrapper<RewardMessage> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(RewardMessage::getIsDelete, 0)
                .eq(RewardMessage::getStatus, RewardMessageStatusEnum.APPROVED.getCode())
                .orderByDesc(RewardMessage::getReviewTime)
                .last("LIMIT " + Math.max(limit, 1));
        return this.list(queryWrapper)
                .stream()
                .map(entity -> {
                    RewardMessageVo vo = new RewardMessageVo();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
