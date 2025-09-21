package com.burger.smartblog.controller;

import com.burger.smartblog.common.BaseResponse;
import com.burger.smartblog.common.ResultUtils;
import com.burger.smartblog.model.entity.SettingConfig;
import com.burger.smartblog.service.SettingConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 网站设置配置接口
 *
 * @author hejiajun
 */
@RestController
@RequestMapping("/api/setting")
@Slf4j
@AllArgsConstructor
public class SettingConfigController {

    private SettingConfigService settingConfigService;

    /**
     * 获取网站设置
     *
     * @return 网站设置配置
     */
    @GetMapping("/site/get")
    public BaseResponse<SettingConfig> getSiteSettings() {
        SettingConfig settings = settingConfigService.getSiteSettings();
        return ResultUtils.success(settings);
    }

    /**
     * 更新网站设置
     *
     * @param settingConfig 网站设置配置
     * @return 是否更新成功
     */
    @PostMapping("/site/update")
    public BaseResponse<Boolean> updateSiteSettings(@RequestBody SettingConfig settingConfig) {
        boolean result = settingConfigService.updateSiteSettings(settingConfig);
        return ResultUtils.success(result);
    }
}