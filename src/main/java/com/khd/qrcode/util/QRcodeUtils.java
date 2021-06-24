package com.khd.qrcode.util;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @description: 带文字的二维码
 * @author: Ycc
 * @since: 2020/7/13
 **/
public class QRcodeUtils {

    public static BufferedImage generateQrCode(String content, String logoPath) {
        BufferedImage image = QrCodeUtil.generate(
                content, // 二维码内容
                QrConfig.create().setImg(logoPath)// 附带logo
                        .setWidth(490) // 二维码的宽
                        .setHeight(490) // 二维码的高
                        .setMargin(0)); // 边距
        return image;
    }


    //区别于旧的方法
    public static BufferedImage createPictureNew(BufferedImage image, String bgPath, String text) {
        try {
            // 首先先画背景图片
            BufferedImage backgroundImage = ImageIO.read(new File(bgPath));
            // 背景图片的宽度
            int bgWidth = backgroundImage.getWidth();
            // 二维码的宽度
            int qrWidth = image.getWidth();
            // 二维码距离背景图片横坐标（X）的距离，居中显示
            int distanceX = (bgWidth - qrWidth) / 2;
            // 二维码距离背景图片纵坐标（Y）的距离
            int distanceY = distanceX;
            // 基于图片backgroundImage对象打开绘图
            Graphics2D rng = backgroundImage.createGraphics();
            rng.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
            rng.drawImage(image, distanceX, distanceY, null);

            //设置字体
            Font font = new Font("宋体", Font.PLAIN, 30);
            rng.setFont(font);
            rng.setColor(Color.black);
            // 获取当前文字的对象
            FontMetrics metrics = rng.getFontMetrics(font);

            // 文字在图片中的坐标 这里设置在中间
            int startX = (bgWidth - metrics.stringWidth(text)) / 2; //当前文字对象到横坐标（X）的距离
            int startY = backgroundImage.getHeight() - 10; //当前文字对象到纵坐标（Y）的距离
            rng.drawString(text, startX, startY);
            // 处理绘图
            rng.dispose();
            image = backgroundImage;
            image.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将绘制好的图片返回
        return image;
    }

}
