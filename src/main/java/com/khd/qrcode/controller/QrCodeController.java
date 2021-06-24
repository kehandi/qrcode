package com.khd.qrcode.controller;

import com.khd.qrcode.util.QRcodeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;

@RestController
//@RequestMapping("QrCode")
public class QrCodeController {

    /**
     * @param shop_id
     * @param shop_name
     * @param response
     * @return void
     * @description: 获取二维码的方法, 通过IO流返回, 不再写入磁盘通过路径展示
     * @author: Ycc
     * @date: 2020/8/12
     */

    @GetMapping("/getShopCode")
    public void getCode(String shop_id, String shop_name, HttpServletResponse response) throws IOException {

        //设置输出文件格式
        response.setContentType("image/png");

        // 存放在二维码中的内容
//        String text = EncryptUtil.encrypt(shop_id) + "_hfyy";
        String text = "kehandi is good";

        //获取二维码logo路径
//        String logoPath = "F:/head/test.jpg";
        String logoPath = "/Users/mac/test.jpg";

        //二维码背景图路径
        String bgPath = "/Users/mac/bg.png";

        //生成二维码
        BufferedImage qrCodeImage = QRcodeUtils.generateQrCode(text, logoPath);
        BufferedImage picture = QRcodeUtils.createPictureNew(qrCodeImage, bgPath, shop_name);

        //获取绘制好的图片的InputStream对象
        InputStream in = getImageStream(picture);
        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        //创建存放文件内容的数组
        byte[] buff = new byte[1024];
        //所读取的内容使用n来接收
        int n;
        //当没有读取完时,继续读取,循环
        while ((n = in.read(buff)) != -1) {
            //将字节数组的数据全部写入到输出流中
            outputStream.write(buff, 0, n);
        }
        //强制将缓存区的数据进行输出
        outputStream.flush();
        //关流
        outputStream.close();
        in.close();
    }

    //从图片文件或BufferedImage中得到InputStream
    public static InputStream getImageStream(BufferedImage bi) {

        InputStream is = null;

        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        ImageOutputStream imOut;
        try {
            imOut = ImageIO.createImageOutputStream(bs);

            ImageIO.write(bi, "png", imOut);

            is = new ByteArrayInputStream(bs.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}