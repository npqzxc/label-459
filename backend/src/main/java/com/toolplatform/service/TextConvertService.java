package com.toolplatform.service;

import com.toolplatform.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TextConvertService {
    private static final Logger logger = LoggerFactory.getLogger(TextConvertService.class);

    public String convert(String text, String type) {
        if (text == null || text.isEmpty()) {
            throw new BusinessException("文本内容不能为空");
        }

        logger.info("Converting text with type: {}", type);

        switch (type.toLowerCase()) {
            case "uppercase":
                return text.toUpperCase();
            case "lowercase":
                return text.toLowerCase();
            case "titlecase":
                return toTitleCase(text);
            default:
                throw new BusinessException("不支持的转换类型: " + type);
        }
    }

    private String toTitleCase(String text) {
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }
}
