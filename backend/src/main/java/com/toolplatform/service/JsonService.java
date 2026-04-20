package com.toolplatform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.toolplatform.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JsonService {
    private static final Logger logger = LoggerFactory.getLogger(JsonService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> processJson(String jsonStr, String action) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new BusinessException("JSON内容不能为空");
        }

        logger.info("Processing JSON with action: {}", action);

        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonStr);
        } catch (JsonProcessingException e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("valid", false);
            errorResult.put("error", "JSON格式错误");
            errorResult.put("errorMessage", e.getOriginalMessage() != null ? e.getOriginalMessage() : e.getMessage());
            return errorResult;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);

        try {
            switch (action.toLowerCase()) {
                case "format":
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                    result.put("result", objectMapper.writeValueAsString(jsonNode));
                    result.put("action", "format");
                    break;
                case "compress":
                    objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
                    result.put("result", objectMapper.writeValueAsString(jsonNode));
                    result.put("action", "compress");
                    break;
                case "validate":
                    result.put("result", "JSON格式正确");
                    result.put("action", "validate");
                    break;
                default:
                    throw new BusinessException("不支持的操作类型: " + action);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("JSON处理失败: " + e.getMessage());
        }

        return result;
    }
}
