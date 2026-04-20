package com.toolplatform.service;

import com.toolplatform.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimestampService {
    private static final Logger logger = LoggerFactory.getLogger(TimestampService.class);
    
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String[] INPUT_FORMATS = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd",
        "yyyyMMddHHmmss",
        "yyyyMMdd"
    };

    public Map<String, Object> processTimestamp(String input, String action, String format) {
        String outputFormat = (format == null || format.isEmpty()) ? DEFAULT_FORMAT : format;
        
        if ("current".equalsIgnoreCase(action)) {
            return getCurrentTimestamp(outputFormat);
        }
        
        if (input == null || input.trim().isEmpty()) {
            throw new BusinessException("输入内容不能为空");
        }
        
        input = input.trim();
        
        logger.info("Processing timestamp: action={}, input={}, format={}", action, input, outputFormat);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (action.toLowerCase()) {
                case "timestamp_to_date":
                    result = timestampToDate(input, outputFormat);
                    break;
                case "date_to_timestamp":
                    result = dateToTimestamp(input);
                    break;
                case "current":
                    result = getCurrentTimestamp();
                    break;
                default:
                    throw new BusinessException("不支持的操作类型: " + action);
            }
        } catch (NumberFormatException e) {
            throw new BusinessException("时间戳格式不正确，请输入有效的数字");
        } catch (Exception e) {
            throw new BusinessException("处理失败: " + e.getMessage());
        }
        
        return result;
    }

    private Map<String, Object> timestampToDate(String input, String format) {
        Map<String, Object> result = new HashMap<>();
        long timestamp;
        
        try {
            timestamp = Long.parseLong(input);
        } catch (NumberFormatException e) {
            throw new BusinessException("时间戳必须是有效的数字");
        }
        
        if (timestamp < 0) {
            throw new BusinessException("时间戳不能为负数");
        }
        
        long milliseconds;
        String unit;
        
        if (timestamp < 10000000000L) {
            milliseconds = timestamp * 1000;
            unit = "秒级 (10位)";
        } else {
            milliseconds = timestamp;
            unit = "毫秒级 (13位)";
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(milliseconds), 
                ZoneId.systemDefault()
            );
            
            result.put("result", dateTime.format(formatter));
            result.put("timestamp", input);
            result.put("unit", unit);
            result.put("format", format);
            result.put("milliseconds", milliseconds);
        } catch (Exception e) {
            throw new BusinessException("日期格式无效: " + format);
        }
        
        return result;
    }

    private Map<String, Object> dateToTimestamp(String input) {
        Map<String, Object> result = new HashMap<>();
        Date date = null;
        String matchedFormat = null;
        
        for (String fmt : INPUT_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(fmt);
                sdf.setLenient(false);
                date = sdf.parse(input);
                matchedFormat = fmt;
                break;
            } catch (ParseException ignored) {
            }
        }
        
        if (date == null) {
            throw new BusinessException(
                "无法解析日期格式，请使用以下格式之一：\n" +
                "yyyy-MM-dd HH:mm:ss, yyyy-MM-dd, \n" +
                "yyyy/MM/dd HH:mm:ss, yyyy/MM/dd, \n" +
                "yyyyMMddHHmmss, yyyyMMdd"
            );
        }
        
        long milliseconds = date.getTime();
        long seconds = milliseconds / 1000;
        
        result.put("timestamp_ms", milliseconds);
        result.put("timestamp_s", seconds);
        result.put("result", String.valueOf(milliseconds));
        result.put("input", input);
        result.put("matchedFormat", matchedFormat);
        
        return result;
    }

    private Map<String, Object> getCurrentTimestamp(String format) {
        Map<String, Object> result = new HashMap<>();
        
        long milliseconds = System.currentTimeMillis();
        long seconds = milliseconds / 1000;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(milliseconds), 
            ZoneId.systemDefault()
        );
        
        String formattedDate = dateTime.format(formatter);
        
        result.put("timestamp_ms", milliseconds);
        result.put("timestamp_s", seconds);
        result.put("result", formattedDate);
        result.put("datetime", formattedDate);
        result.put("format", format);
        
        return result;
    }
}
