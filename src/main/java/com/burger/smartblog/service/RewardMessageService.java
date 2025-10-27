package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.dto.reward.RewardMessageCreateRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageQueryRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageReviewRequest;
import com.burger.smartblog.model.entity.RewardMessage;
import com.burger.smartblog.model.vo.RewardMessageVo;

import java.util.List;

public interface RewardMessageService extends IService<RewardMessage> {

    Long submitRewardMessage(RewardMessageCreateRequest request);

    boolean reviewRewardMessage(Long id, RewardMessageReviewRequest request);

    Page<RewardMessage> getRewardMessagePage(RewardMessageQueryRequest request);

    List<RewardMessageVo> listApprovedMessages(int limit);
}
