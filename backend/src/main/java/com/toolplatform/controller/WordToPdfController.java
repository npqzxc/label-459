package com.toolplatform.controller;

import com.toolplatform.dto.ApiResponse;
import com.toolplatform.service.WordToPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/word")
public class WordToPdfController {

    @Autowired
    private WordToPdfService wordToPdfService;

    @PostMapping("/convert")
    public ApiResponse<Map<String, String>> convertToPdf(@RequestParam("file") MultipartFile file) {
        String filename = wordToPdfService.convertToPdf(file);
        Map<String, String> data = new HashMap<>();
        data.put("filename", filename);
        data.put("downloadUrl", "/api/word/download/" + filename);
        return ApiResponse.success(data);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable String filename) {
        File file = wordToPdfService.getFile(filename);
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }
}
