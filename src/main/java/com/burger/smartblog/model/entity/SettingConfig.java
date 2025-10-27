package com.burger.smartblog.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 网站设置配置实体
 * 对应表: setting_config
 */
@TableName(value = "setting_config")
@Data
public class SettingConfig implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 网站名称
     */
    private String siteName;

    /**
     * 网站描述
     */
    private String siteDescription;

    /**
     * 网站关键词
     */
    private String siteKeywords;

    /**
     * 网站Logo URL
     */
    private String siteLogo;

    /**
     * 网站图标 URL
     */
    private String favicon;

    /**
     * 关于页面标题
     */
    private String aboutTitle;

    /**
     * 关于页面内容
     */
    private String aboutContent;

    /**
     * 关于页面图片 URL
     */
    private String aboutImage;

    /**
     * SEO标题
     */
    private String seoTitle;

    /**
     * SEO描述
     */
    private String seoDescription;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * GitHub链接
     */
    private String githubUrl;

    /**
     * Twitter链接
     */
    private String twitterUrl;

    /**
     * LinkedIn链接
     */
    private String linkedinUrl;

    /**
     * 联系邮箱
     */
    private String emailContact;

    /**
     * 微信二维码链接
     */
    private String wechatQrUrl;

    /**
     * 微信支付二维码链接
     */
    private String wechatPayQrUrl;

    /**
     * 公众号二维码链接
     */
    private String wechatOfficialQrUrl;

    /**
     * 支付宝二维码链接
     */
    private String alipayQrUrl;

    /**
     * 启用评论功能
     */
    private Boolean enableComments = true;

    /**
     * 启用搜索功能
     */
    private Boolean enableSearch = true;

    /**
     * 启用深色模式
     */
    private Boolean enableDarkMode = true;

    /**
     * 每页文章数量
     */
    private Integer articlesPerPage = 10;

    /**
     * Google Analytics ID
     */
    private String googleAnalyticsId;

    /**
     * 百度统计ID
     */
    private String baiduAnalyticsId;

    /**
     * AI 聊天快捷键（例如：Alt+K、Ctrl+Shift+J）
     */
    private String aiChatShortcut;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
