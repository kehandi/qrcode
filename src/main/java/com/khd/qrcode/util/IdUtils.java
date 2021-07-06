package com.khd.qrcode.util;

import java.util.UUID;

/**
 * ID生成器工具类
 *
 * @author kehandi
 * @title: IdUtils
 * @projectName quzhi-admin-web
 * @date 2020/11/16 11:41
 */
public class IdUtils {
    /**
     * 获取随机UUID
     *
     * @return 随机UUID
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }


    public static void main(String[] args) {

        System.out.println(randomUUID());
    }
}
