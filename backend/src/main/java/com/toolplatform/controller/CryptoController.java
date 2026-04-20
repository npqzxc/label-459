package com.toolplatform.controller;

import com.toolplatform.dto.ApiResponse;
import com.toolplatform.dto.CryptoRequest;
import com.toolplatform.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    @Autowired
    private CryptoService cryptoService;

    @PostMapping("/process")
    public ApiResponse<Map<String, String>> process(@RequestBody CryptoRequest request) {
        String result = cryptoService.process(
                request.getText(),
                request.getAlgorithm(),
                request.getAction(),
                request.getKey()
        );
        Map<String, String> data = new HashMap<>();
        data.put("result", result);
        return ApiResponse.success(data);
    }
}
