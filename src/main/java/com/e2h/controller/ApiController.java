package com.e2h.controller;

import com.e2h.entity.GenerationEntity;
import com.e2h.request.GenerationRequest;
import com.e2h.service.ApiService;
import com.sun.istack.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApiController {

    @Autowired
    ApiService apiService;

    @PostMapping("/generate")
    public ResponseEntity<GenerationEntity> preview(@NotNull @RequestBody GenerationRequest request){
        return ResponseEntity.ok(apiService.preview(request));
    }

    @PostMapping("/get")
    public ResponseEntity<GenerationEntity> get(@NotNull @RequestParam String id){
        return ResponseEntity.ok(apiService.get(id));
    }

    @PostMapping("/download/html")
    public ResponseEntity<Resource> downloadHtml(@NotNull @RequestParam String id){
        return ResponseEntity.ok().header("filename", "Data.html").body(apiService.downloadHtml(id));
    }

    @PostMapping("/download/config")
    public ResponseEntity<Resource> downloadConfig(@NotNull @RequestParam String id){
        return ResponseEntity.ok().header("filename", "Config.json").body(apiService.downloadConfig(id));
    }

}

