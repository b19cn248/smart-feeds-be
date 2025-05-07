package com.olh.feeds.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/build")
@RestController
public class BuilderController {

    @PostMapping
    public String healthCheck() {
        try {
            // Sử dụng đường dẫn tuyệt đối đến sh hoặc bash
            Process process = Runtime.getRuntime().exec("/bin/sh /home/linh2307_hmu/smart-feeds-be/run.sh");

            // Đợi script hoàn thành
            int exitCode = process.waitFor();
            return "Success với mã thoát: " + exitCode;
        } catch (Exception e) {
            // Log lỗi để dễ debug
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }

}
