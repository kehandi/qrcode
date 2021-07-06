package com.khd.qrcode.util;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.khd.qrcode.entity.QrCodeEntity;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @description: 带文字的二维码
 * @author: Ycc
 * @since: 2020/7/13
 **/
@Slf4j
public class QRcodeUtils {

    public static BufferedImage generateQrCode(String content, String logoPath) {
        BufferedImage image = QrCodeUtil.generate(
                content, // 二维码内容
                QrConfig.create()//.setImg(logoPath)// 附带logo
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

    /**
     * 获取二维码压缩包
     *
     * @param response
     * @param batchId  批次号
     * @param list     二维码字符串列表
     */
    public static void getCodeZip(HttpServletResponse response, String batchId, List<QrCodeEntity> list) {
        // 生成二维码图片
        response.setContentType("application/octet-stream");
        // 压缩包名
        response.setHeader("Content-Disposition", "attachment; filename=QrCode-" + batchId + ".zip");
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(response.getOutputStream());
            // zos.setLevel(5);//压缩等级

            for (int j = 0; j < list.size(); j++) {
                // 获取二维码字符串
                String codeString = list.get(j).getUuid();
                // 生成二维码图片
                BufferedImage qrCode = createQrCode(batchId, codeString, 900);
                // 将bufferedImage转成inputStream
                InputStream inputStream = bufferedImageToInputStream(qrCode);

                // 异常直接跳过
                if (inputStream == null) {
                    continue;
                }

                // 压缩文件名称 设置ZipEntry对象
                zos.putNextEntry(new ZipEntry(j + 1 + ".JPEG"));

                int temp;
                // 读取内容
                while ((temp = inputStream.read()) != -1) {
                    // 压缩输出
                    zos.write(temp);
                }
                // 关闭输入流
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != zos) {
                    zos.flush();
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成包含字符串信息的二维码图片
     *
     * @param batchId    二维码批次号
     * @param content    二维码携带信息
     * @param qrCodeSize 二维码图片大小
     * @throws WriterException
     */
    public static BufferedImage createQrCode(String batchId, String content, int qrCodeSize)
            throws WriterException {
        // 设置二维码纠错级别Map
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        // 矫错级别
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // 创建比特矩阵(位矩阵)的QR码编码的字符串
        BitMatrix byteMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
        // 使BufferedImage勾画QRCode (matrixWidth 是行二维码像素点)
        int matrixWidth = byteMatrix.getWidth();
        int picWidth = matrixWidth - 200;
        // 除掉二维码过长的底部留白
        int picHeight = matrixWidth - 150;
        BufferedImage image = new BufferedImage(picWidth, picHeight, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        // 画二维码
        graphics.setColor(Color.WHITE);
        // 画刷填充矩形
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // 使用比特矩阵画并保存图像
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i - 100, j - 100, 1, 1);
                }
            }
        }
        graphics.setColor(Color.BLACK);
        // 设置底部文字
        Font font = new Font("黑体", Font.BOLD, 32);
        graphics.setFont(font);
        // 消除文字锯齿
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 计算文字长度，计算居中的x点坐标
        FontMetrics fm = graphics.getFontMetrics(font);
        int textWidth = fm.stringWidth(content);
        int widthX = (picWidth - textWidth) / 2;
        graphics.drawString(content, widthX, 719);

        // 计算文字长度，计算居中的x点坐标
        int textWidth2 = fm.stringWidth("批次号:" + batchId);
        int widthX2 = (picWidth - textWidth2) / 2;
        graphics.drawString("批次号:" + batchId, widthX2, 746);
        graphics.dispose();
        // 设置图片大小，并在底部加上字
        return resize(image, 400, 400);
    }

    /**
     * 设置图片大小，并在底部加上字
     *
     * @param img
     * @param newW
     * @param newH
     * @return
     */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return dimg;
    }

    /**
     * 将BufferedImage转换为InputStream
     *
     * @param image
     * @return
     */
    public static InputStream bufferedImageToInputStream(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            return new ByteArrayInputStream(os.toByteArray());
        } catch (IOException e) {
            log.error("提示:", e);
        }
        return null;
    }


}
