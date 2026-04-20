package com.toolplatform.controller;

import com.toolplatform.dto.ApiResponse;
import com.toolplatform.dto.TextConvertRequest;
import com.toolplatform.service.TextConvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/text")
public class TextConvertController {

    @Autowired
    private TextConvertService textConvertService;

    @PostMapping("/convert")
    public ApiResponse<Map<String, String>> convert(@RequestBody TextConvertRequest request) {
        String result = textConvertService.convert(request.getText(), request.getType());
        Map<String, String> data = new HashMap<>();
        data.put("result", result);
        return ApiResponse.success(data);
    }
}
