package com.toolplatform.controller;

import com.toolplatform.dto.ApiResponse;
import com.toolplatform.dto.JsonRequest;
import com.toolplatform.service.JsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/json")
public class JsonController {

    @Autowired
    private JsonService jsonService;

    @PostMapping("/process")
    public ApiResponse<Map<String, Object>> process(@RequestBody JsonRequest request) {
        Map<String, Object> result = jsonService.processJson(request.getJson(), request.getAction());
        return ApiResponse.success(result);
    }
}
