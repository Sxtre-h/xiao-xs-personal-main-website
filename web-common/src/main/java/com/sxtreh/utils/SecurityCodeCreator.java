package com.sxtreh.utils;

import java.util.Random;

/**
 * 生成指定位数的随机字符串
 */
public class SecurityCodeCreator {

    private static final Integer strType = 3;
    private static final String originLowerStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String originCapitalStr = "abcdefhijklmnopqrstuvwxyz";
    private static final String originNumberStr = "0123456789";

    public static String getCode(Integer lenth) {
        String code = "";
        for (int i = 0; i < lenth; i++) {
            Random random = new Random();
            switch (random.nextInt(strType)) {
                case 0 -> {
                    code += originNumberStr.charAt(random.nextInt(originNumberStr.length()));
                }
                case 1 -> {
                    code += originCapitalStr.charAt(random.nextInt(originCapitalStr.length()));

                }
                case 2 -> {
                    code += originLowerStr.charAt(random.nextInt(originLowerStr.length()));
                }
            }
        }
        return code;
    }
}
