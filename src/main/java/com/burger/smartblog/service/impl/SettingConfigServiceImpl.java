package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.mapper.SettingConfigMapper;
import com.burger.smartblog.model.entity.SettingConfig;
import com.burger.smartblog.service.SettingConfigService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * @author hejiajun
 * @description 针对表【setting_config】的数据库操作Service实现
 */
@Service
public class SettingConfigServiceImpl extends ServiceImpl<SettingConfigMapper, SettingConfig>
    implements SettingConfigService {

    @Override
    public SettingConfig getSiteSettings() {
        // 获取第一条记录作为网站设置
        LambdaQueryWrapper<SettingConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public boolean updateSiteSettings(SettingConfig settingConfig) {
        // 如果没有记录则插入，否则更新第一条记录
        SettingConfig existing = getSiteSettings();
        if (existing == null) {
            return this.save(settingConfig);
        } else {
            settingConfig.setId(existing.getId());
            return this.updateById(settingConfig);
        }
    }
}