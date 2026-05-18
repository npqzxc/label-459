package com.toolplatform.service;

import com.toolplatform.exception.BusinessException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class CryptoService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);
    private static final String AES_ALGORITHM = "AES";

    public String process(String text, String algorithm, String action, String key) {
        if (text == null || text.isEmpty()) {
            throw new BusinessException("文本内容不能为空");
        }

        logger.info("Processing crypto with algorithm: {}, action: {}", algorithm, action);

        switch (algorithm.toLowerCase()) {
            case "base64":
                return processBase64(text, action);
            case "md5":
                return processMd5(text);
            case "aes":
                return processAes(text, action, key);
            default:
                throw new BusinessException("不支持的算法: " + algorithm);
        }
    }

    private String processBase64(String text, String action) {
        try {
            if ("encode".equals(action)) {
                return Base64.encodeBase64String(text.getBytes(StandardCharsets.UTF_8));
            } else if ("decode".equals(action)) {
                byte[] decoded = Base64.decodeBase64(text);
                return new String(decoded, StandardCharsets.UTF_8);
            } else {
                throw new BusinessException("Base64只支持encode和decode操作");
            }
        } catch (Exception e) {
            logger.error("Base64 processing failed", e);
            throw new BusinessException("Base64处理失败: " + e.getMessage());
        }
    }

    private String processMd5(String text) {
        try {
            return DigestUtils.md5Hex(text);
        } catch (Exception e) {
            logger.error("MD5 processing failed", e);
            throw new BusinessException("MD5处理失败: " + e.getMessage());
        }
    }

    private String processAes(String text, String action, String key) {
        if (key == null || key.isEmpty()) {
            throw new BusinessException("AES加密需要提供密钥");
        }

        try {
            // 确保密钥长度为16字节（128位）
            String paddedKey = padKey(key, 16);
            SecretKeySpec secretKey = new SecretKeySpec(paddedKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

            if ("encrypt".equals(action)) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
                return Base64.encodeBase64String(encrypted);
            } else if ("decrypt".equals(action)) {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decoded = Base64.decodeBase64(text);
                byte[] decrypted = cipher.doFinal(decoded);
                return new String(decrypted, StandardCharsets.UTF_8);
            } else {
                throw new BusinessException("AES只支持encrypt和decrypt操作");
            }
        } catch (Exception e) {
            logger.error("AES processing failed", e);
            throw new BusinessException("AES处理失败，请检查密钥和输入格式");
        }
    }

    private String padKey(String key, int length) {
        if (key.length() >= length) {
            return key.substring(0, length);
        }
        StringBuilder sb = new StringBuilder(key);
        while (sb.length() < length) {
            sb.append("0");
        }
        return sb.toString();
    }
}
