package com.grzeng.api.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.api.utils.MD5Utils </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/3 13:56 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class MD5Utils {

    private static final char[] HEX_DIGITS = {'0','1','2','3','4','5','6','7','8','9',
            'a','b','c','d','e','f'};

    /**
     * MD5 加密（默认返回 32 位小写）
     *
     * @param str 待加密的字符串
     * @return 32位小写MD5
     */
    public static String md5(String str) {
        return md5(str, false);
    }

    /**
     * MD5 加密
     *
     * @param str       待加密字符串
     * @param is16bit   true 返回16位，false 返回32位
     * @return MD5密文
     */
    public static String md5(String str, boolean is16bit) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            String md5_32 = bytesToHex(md.digest());

            if (is16bit) {
                // 16位就是 32位的第8~24位（去掉前8位和后8位）
                return md5_32.substring(8, 24);
            }
            return md5_32;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密出错", e);
        }
    }

    /**
     * MD5 加密 + 加盐（推荐！更安全）
     *
     * @param str  原始字符串
     * @param salt 盐值（建议随机生成并保存）
     * @return 32位小写MD5
     */
    public static String md5WithSalt(String str, String salt) {
        if (str == null) return null;
        if (salt == null || salt.isEmpty()) {
            return md5(str);
        }
        // 常见加盐方式：salt + str  或  str + salt  或前后都加
        return md5(salt + str);
        // 也可以 return md5(str + salt);
        // 更强推荐：return md5(md5(str) + salt);  // 双重MD5+盐
    }

    /**
     * 字节数组转16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS[(b >> 4) & 0x0f]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString().toUpperCase();
    }

    // ====================== 测试 ======================
    public static void main(String[] args) {
        String password = "1";
        String salt = "x7k9m2p"; // 实际项目中建议随机生成并保存到数据库

        System.out.println("原始密码: " + password);
        System.out.println("32位MD5: " + md5(password));                    // 32位
        System.out.println("16位MD5: " + md5(password, true));             // 16位
        System.out.println("MD5+盐: " + md5WithSalt(password, salt));      // 加盐
        System.out.println("双重MD5+盐: " + md5(md5(password) + salt));    // 更强
    }
}
