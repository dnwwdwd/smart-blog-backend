package com.burger.smartblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.burger.smartblog.model.entity.SettingConfig;

/**
 * @author hejiajun
 * @description 针对表【setting_config】的数据库操作Service
 */
public interface SettingConfigService extends IService<SettingConfig> {

    /**
     * 获取网站设置
     * @return 网站设置配置
     */
    SettingConfig getSiteSettings();

    /**
     * 更新网站设置
     * @param settingConfig 网站设置配置
     * @return 是否更新成功
     */
    boolean updateSiteSettings(SettingConfig settingConfig);
}