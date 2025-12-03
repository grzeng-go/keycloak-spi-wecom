package com.grzeng.api.utils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.api.utils.RSAUtils </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/3 13:49 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class RSAUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String SIGN_ALGORITHM_SHA256 = "SHA256withRSA";
    private static final String SIGN_ALGORITHM_SHA1   = "SHA1withRSA";   // 兼容旧系统
    private static final String SIGN_ALGORITHM_SHA512 = "SHA512withRSA";
    // 默认使用最安全的 SHA256withRSA
    private static final String DEFAULT_SIGN_ALGORITHM = SIGN_ALGORITHM_SHA256;
    private static final int KEY_SIZE = 2048; // 默认密钥长度，可根据需要调整

    /**
     * 生成 RSA 密钥对（返回 Base64 编码的公钥和私钥）
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥加密
     *
     * @param data      待加密的明文
     * @param publicKey Base64 编码的公钥
     * @return Base64 编码的密文
     */
    public static String encryptByPublicKey(String data, String publicKey) throws Exception {
        PublicKey pubKey = getPublicKeyFromBase64(publicKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptInSegments(cipher, dataBytes);

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 私钥解密
     *
     * @param encryptedData Base64 编码的密文
     * @param privateKey    Base64 编码的私钥
     * @return 明文字符串
     */
    public static String decryptByPrivateKey(String encryptedData, String privateKey) throws Exception {
        PrivateKey priKey = getPrivateKeyFromBase64(privateKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, priKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = decryptInSegments(cipher, encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * 私钥加密（常用于数字签名）
     */
    public static String encryptByPrivateKey(String data, String privateKey) throws Exception {
        PrivateKey priKey = getPrivateKeyFromBase64(privateKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, priKey);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptInSegments(cipher, dataBytes);

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * 公钥解密（对应私钥加密）
     */
    public static String decryptByPublicKey(String encryptedData, String publicKey) throws Exception {
        PublicKey pubKey = getPublicKeyFromBase64(publicKey);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, pubKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = decryptInSegments(cipher, encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // ==================== 分段加密解密（解决单次加密长度限制） ====================

    private static byte[] encryptInSegments(Cipher cipher, byte[] data) throws Exception {
        int inputLen = data.length;
        int maxEncryptBlock = KEY_SIZE / 8 - 11; // PKCS1Padding 下最大明文长度
        byte[] result = new byte[0];
        int offSet = 0;

        while (inputLen - offSet > 0) {
            int segmentSize = Math.min(inputLen - offSet, maxEncryptBlock);
            byte[] cache = new byte[segmentSize];
            System.arraycopy(data, offSet, cache, 0, segmentSize);
            byte[] encryptedSegment = cipher.doFinal(cache);

            // 合并结果
            byte[] temp = new byte[result.length + encryptedSegment.length];
            System.arraycopy(result, 0, temp, 0, result.length);
            System.arraycopy(encryptedSegment, 0, temp, result.length, encryptedSegment.length);
            result = temp;

            offSet += segmentSize;
        }
        return result;
    }

    private static byte[] decryptInSegments(Cipher cipher, byte[] data) throws Exception {
        int inputLen = data.length;
        int maxDecryptBlock = KEY_SIZE / 8; // 密文块大小固定为密钥长度字节数
        byte[] result = new byte[0];
        int offSet = 0;

        while (inputLen - offSet > 0) {
            int segmentSize = Math.min(inputLen - offSet, maxDecryptBlock);
            byte[] cache = new byte[segmentSize];
            System.arraycopy(data, offSet, cache, 0, segmentSize);
            byte[] decryptedSegment = cipher.doFinal(cache);

            // 合并结果
            byte[] temp = new byte[result.length + decryptedSegment.length];
            System.arraycopy(result, 0, temp, 0, result.length);
            System.arraycopy(decryptedSegment, 0, temp, result.length, decryptedSegment.length);
            result = temp;

            offSet += segmentSize;
        }
        return result;
    }

    /**
     * 私钥签名（推荐 SHA256withRSA）
     */
    public static String sign(String data, String privateKeyBase64) throws Exception {
        return sign(data, privateKeyBase64, DEFAULT_SIGN_ALGORITHM);
    }

    /**
     * 私钥签名（可自定义签名算法）
     */
    public static String sign(String data, String privateKeyBase64, String signAlgorithm) throws Exception {
        PrivateKey privateKey = getPrivateKeyFromBase64(privateKeyBase64);
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initSign(privateKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signBytes);
    }

    /**
     * 公钥验签（推荐 SHA256withRSA）
     */
    public static boolean verify(String data, String publicKeyBase64, String signBase64) throws Exception {
        return verify(data, publicKeyBase64, signBase64, DEFAULT_SIGN_ALGORITHM);
    }

    /**
     * 公钥验签（可自定义签名算法）
     */
    public static boolean verify(String data, String publicKeyBase64, String signBase64, String signAlgorithm) throws Exception {
        PublicKey publicKey = getPublicKeyFromBase64(publicKeyBase64);
        Signature signature = Signature.getInstance(signAlgorithm);
        signature.initVerify(publicKey);
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(signBase64);
        return signature.verify(signBytes);
    }

    // ==================== 密钥转换工具 ====================

    public static PublicKey getPublicKeyFromBase64(String publicKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKeyFromBase64(String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    // ==================== 测试主函数 ====================

    public static void main(String[] args) throws Exception {
        // 1. 生成密钥对
        KeyPair keyPair = generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        System.out.println("公钥: " + publicKey);
        System.out.println("私钥: " + privateKey);

        // 2. 公钥加密 -> 私钥解密（最常用场景）
        String originalText = "Hello RSA 这是中文测试！1234567890";
        String encrypted = encryptByPublicKey(originalText, publicKey);
        String decrypted = decryptByPrivateKey(encrypted, privateKey);

        System.out.println("明文: " + originalText);
        System.out.println("密文: " + encrypted);
        System.out.println("解密后: " + decrypted);
        System.out.println("解密是否成功: " + originalText.equals(decrypted));

        // 2. 待签名数据
        String data = "orderId=20251203123456&amount=999.00&timestamp=1733212345";

        // 3. 私钥签名（SHA256withRSA）
        String signature = sign(data, privateKey);
        System.out.println("\n签名串（Base64）:\n" + signature);

        // 4. 公钥验签
        boolean isValid = verify(data, publicKey, signature);
        System.out.println("验签结果: " + (isValid ? "成功" : "失败"));

        // 5. 篡改数据后验签（应该失败）
        String tamperData = data + "&hack=1";
        boolean isTamperValid = verify(tamperData, publicKey, signature);
        System.out.println("篡改后验签: " + (isTamperValid ? "成功" : "失败（正确）"));
    }
}