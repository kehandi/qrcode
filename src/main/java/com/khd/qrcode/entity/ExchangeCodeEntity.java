package com.khd.qrcode.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 兑换码
 *
 * @author kehandi
 * @title: ExchangeCodeEntity
 * @projectName admin-web
 * @date 2021/7/6 8:32 下午
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExchangeCodeEntity {
    /**
     * 自增ID
     */
    private Integer id;

    /**
     * 批次号
     */
    private String batchCode;

    /**
     * 唯一识别码
     */
    private String identifyCode;

    /**
     * 投资商ID
     */
    private Integer investorId;

    /**
     * 投资商名称
     */
    private String investorName;

    /**
     * 项目ID
     */
    private Integer projectId;
    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 单张价格
     */
    private Double price;

    /**
     * 失效日期
     */
    private String expiringTime;

    /**
     * 二维码状态 1未使用 2已使用 3已过期
     */
    private Byte codeStatus;

    /**
     * 使用时间
     */
    private String usedTime;

    /**
     * 使用者账号
     */
    private Integer accountId;

    /**
     * 使用者手机号
     */
    private String accountPhone;

    /**
     * 充值订单号
     */
    private String orderId;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 修改时间
     */
    private String updateTime;

    /*--------------------------------以下是特殊内容------------------------------------*/

    /**
     * 二维码内容
     */
    private String qrcodeContent;
}
