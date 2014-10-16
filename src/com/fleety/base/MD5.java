package com.fleety.base;

import java.security.MessageDigest;

public class MD5 {
    private final static String[] aryhHexDigits = { "0", "1", "2", "3", "4",
            "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b&0xFF;
        int d1 = n / 16;
        int d2 = n % 16;
        return aryhHexDigits[d1] + aryhHexDigits[d2];
    }

    private static MessageDigest md = null;
    /**
     * 得到md5加密后的十六进制字符串
     * 
     * @param origin
     */
    public static String getHexDigest(String origin) {
        return getHexDigest(origin.getBytes());
    }
    public synchronized static String getHexDigest(byte[] b1) {
        if(md == null){
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (Exception ex) {
                return null;
            }
        }
        String resultString = byteArrayToHexString(md.digest(b1));

        return resultString;
    }
}
