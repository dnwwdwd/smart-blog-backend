package com.burger.smartblog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.dto.reward.RewardMessageCreateRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageQueryRequest;
import com.burger.smartblog.model.dto.reward.RewardMessageReviewRequest;
import com.burger.smartblog.model.entity.RewardMessage;
import com.burger.smartblog.model.entity.SettingConfig;
import com.burger.smartblog.model.vo.RewardMessageVo;
import com.burger.smartblog.model.vo.RewardPayConfigVo;
import com.burger.smartblog.service.RewardMessageService;
import com.burger.smartblog.service.SettingConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reward")
@RequiredArgsConstructor
public class RewardMessageController {

    private final RewardMessageService rewardMessageService;
    private final SettingConfigService settingConfigService;

    /**
     * 用户提交留言打赏
     */
    @PostMapping("/message")
    public BaseResponse<Long> submitRewardMessage(@RequestBody @Valid RewardMessageCreateRequest request) {
        Long id = rewardMessageService.submitRewardMessage(request);
        return ResultUtils.success(id);
    }

    /**
     * 管理端分页查询留言
     */
    @PostMapping("/message/page")
    public BaseResponse<Page<RewardMessage>> getRewardMessages(@RequestBody RewardMessageQueryRequest request) {
        return ResultUtils.success(rewardMessageService.getRewardMessagePage(request));
    }

    /**
     * 审核留言
     */
    @PostMapping("/message/review/{id}")
    public BaseResponse<Boolean> reviewRewardMessage(@PathVariable Long id,
                                                     @RequestBody @Valid RewardMessageReviewRequest request) {
        return ResultUtils.success(rewardMessageService.reviewRewardMessage(id, request));
    }

    /**
     * 前端获取审核通过的打赏留言
     */
    @GetMapping("/message/approved")
    public BaseResponse<List<RewardMessageVo>> listApprovedMessages(@RequestParam(defaultValue = "5") int limit) {
        return ResultUtils.success(rewardMessageService.listApprovedMessages(limit));
    }

    /**
     * 获取打赏支付二维码配置
     */
    @GetMapping("/pay/config")
    public BaseResponse<RewardPayConfigVo> getRewardPayConfig() {
        SettingConfig settings = settingConfigService.getSiteSettings();
        RewardPayConfigVo configVo = new RewardPayConfigVo();
        if (settings != null) {
            configVo.setWechatPayQrUrl(settings.getWechatPayQrUrl());
            configVo.setAlipayQrUrl(settings.getAlipayQrUrl());
        }
        return ResultUtils.success(configVo);
    }
}
