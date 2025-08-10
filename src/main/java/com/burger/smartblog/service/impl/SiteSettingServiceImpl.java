package com.burger.smartblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.burger.smartblog.model.entity.SiteSetting;
import com.burger.smartblog.service.SiteSettingService;
import com.burger.smartblog.mapper.SiteSettingMapper;
import org.springframework.stereotype.Service;

/**
* @author hejiajun
* @description 针对表【site_settings】的数据库操作Service实现
* @createDate 2025-08-10 11:45:33
*/
@Service
public class SiteSettingServiceImpl extends ServiceImpl<SiteSettingMapper, SiteSetting>
    implements SiteSettingService {

}




