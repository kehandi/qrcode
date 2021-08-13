package com.khd.qrcode.util;

import cn.hutool.extra.qrcode.BufferedImageLuminanceSource;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.khd.qrcode.entity.ExchangeCodeEntity;
import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 二维码登录
 *
 * @author ZhouBing
 * @title: QrCodeUtil
 * @projectName quzhi-admin-web
 * @date 2021-05-26 14:10
 */
@Slf4j
public class QrCodeUtil {

    /**
     * 功能描述: 生成二维码 BufferedImage.
     *
     * @param content
     * @param qrWidth
     * @param qrHeight
     * @return java.awt.image.BufferedImage
     * @author zb
     * @date 2021-05-26 14:10
     */
    public static BufferedImage getBufferImage(String content, int qrWidth, int qrHeight) throws Exception {
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, qrWidth, qrHeight, hints);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        log.debug("执行生成二维码 BufferedImage操作");
        return image;
    }

    /**
     * 功能描述:  生成base64格式二维码.
     *
     * @param content  content
     * @param qrWidth  qrWidth
     * @param qrHeight qrHeight
     * @return string
     * @author zb
     * @date 2021-05-26 14:10
     */
    public static String getBase64(String content, int qrWidth, int qrHeight) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            BufferedImage image = getBufferImage(content, qrWidth, qrHeight);
            //转换成png格式的IO流
            ImageIO.write(image, "png", byteArrayOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bytes = byteArrayOutputStream.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        String base64 = encoder.encodeBuffer(bytes).trim();
        base64 = "data:image/png;base64," + base64;
        log.debug("执行生成base64格式二维码操作");
        return base64;
    }


    public static String deEncodeByBase64(String baseStr) {
        String content = null;
        BufferedImage image;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b = null;
        try {
            int i = baseStr.indexOf("data:image/png;base64,");
            baseStr = baseStr.substring(i + "data:image/png;base64,".length());//去掉base64图片的data:image/png;base64,部分才能转换为byte[]

            b = decoder.decodeBuffer(baseStr);//baseStr转byte[]
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);//byte[] 转BufferedImage
            image = ImageIO.read(byteArrayInputStream);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            Binarizer binarizer = new HybridBinarizer(source);
            BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            Result result = new MultiFormatReader().decode(binaryBitmap, hints);//解码
            System.out.println("图片中内容：  ");
            System.out.println("content： " + result.getText());
            content = result.getText();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return content;
    }

    /**
     * 获取二维码压缩包
     *
     * @param response
     * @param batchCode 批次号
     * @param list      二维码字符串列表
     */
    public static void getExchangeCodeZip(HttpServletResponse response, String batchCode, List<ExchangeCodeEntity> list) {
        // 生成二维码图片
        response.setContentType("application/octet-stream");
        // 压缩包名
        response.setHeader("Content-Disposition", "attachment; filename=QrCode-" + batchCode + ".zip");
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(response.getOutputStream());
            // zos.setLevel(5);//压缩等级

            for (int j = 0; j < list.size(); j++) {
                // 获取二维码字符串
                String codeString = list.get(j).getIdentifyCode();
                // 生成二维码图片
                BufferedImage qrCode = createQrCode(batchCode, codeString, 900);
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
     * @param batchId      二维码批次号
     * @param identifyCode 二维码携带信息
     * @param qrCodeSize   二维码图片大小
     * @throws WriterException
     */
    public static BufferedImage createQrCode(String batchId, String identifyCode, int qrCodeSize)
            throws WriterException {
        // 设置二维码纠错级别Map
        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        // 矫错级别
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // 创建比特矩阵(位矩阵)的QR码编码的字符串
        BitMatrix byteMatrix = qrCodeWriter.encode("KLCXKJ-Recharge," + identifyCode, BarcodeFormat.QR_CODE, qrCodeSize, qrCodeSize, hintMap);
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
        Font font = new Font("黑体", Font.BOLD, 30);
        graphics.setFont(font);
        // 消除文字锯齿
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // 计算文字长度，计算居中的x点坐标
        FontMetrics fm = graphics.getFontMetrics(font);
//        int textWidth = fm.stringWidth(content);
//        int widthX = (picWidth - textWidth) / 2;
//        graphics.drawString(content, widthX, 719);
//
//        // 计算文字长度，计算居中的x点坐标
//        int textWidth2 = fm.stringWidth("批次号:" + batchId);
//        int widthX2 = (picWidth - textWidth2) / 2;
//        graphics.drawString("批次号:" + batchId, widthX2, 746);

        // 计算文字长度，计算居中的x点坐标
        int textWidth1 = fm.stringWidth("批次号:" + batchId);
        int widthX1 = (picWidth - textWidth1) / 2;
        graphics.drawString("批次号:" + batchId, widthX1, 719);

        // 支付凭证
        int textWidth2 = fm.stringWidth("支付凭证:" + identifyCode);
        int widthX2 = (picWidth - textWidth2) / 2;
        graphics.drawString("支付凭证:" + identifyCode, widthX2, 746);

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

    /**
     * 功能描述:  test.
     *
     * @param
     * @return
     * @author ZhouBing
     * @date 2021-05-26 14:10
     */
    public static void main(String[] args) throws Exception {
//          ImageIO.write(getBufferImage(UUID.randomUUID().toString(), 500, 500), "jpg", new File("/Users/zhoubing/Downloads/qrCode.jpg"));
        System.out.println("--base64--" + getBase64(UUID.randomUUID().toString() + System.currentTimeMillis(), 500, 500));
        deEncodeByBase64(getBase64(UUID.randomUUID().toString() + System.currentTimeMillis(), 500, 500));

    }
}

