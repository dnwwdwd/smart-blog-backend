package com.burger.smartblog.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 打赏支付配置 VO
 */
@Data
public class RewardPayConfigVo implements Serializable {

    /**
     * 微信支付二维码链接
     */
    private String wechatPayQrUrl;

    /**
     * 支付宝二维码链接
     */
    private String alipayQrUrl;

    @Serial
    private static final long serialVersionUID = 1L;
}
