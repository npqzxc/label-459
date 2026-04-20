package com.toolplatform.controller;

import com.toolplatform.dto.ApiResponse;
import com.toolplatform.dto.TimestampRequest;
import com.toolplatform.service.TimestampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/timestamp")
public class TimestampController {

    @Autowired
    private TimestampService timestampService;

    @PostMapping("/process")
    public ApiResponse<Map<String, Object>> process(@RequestBody TimestampRequest request) {
        Map<String, Object> result = timestampService.processTimestamp(
            request.getInput(), 
            request.getAction(),
            request.getFormat()
        );
        return ApiResponse.success(result);
    }
}
