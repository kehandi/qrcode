package com.khd.qrcode.entity;

import lombok.Data;

/**
 * 二维码实体
 *
 * @author kehandi
 * @title: QRCodeEntity
 * @projectName qrcode
 * @date 2021/7/6 10:22 上午
 */
@Data
public class QrCodeEntity {

    private String uuid;

    public QrCodeEntity(String uuid) {
        this.uuid = uuid;
    }
}
