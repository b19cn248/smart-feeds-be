package com.olh.feeds.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/build")
@RestController
public class BuilderController {

    // Test API to check if the server is running
    @PostMapping
    public String healthCheck() {
        try {
            // Đảm bảo file script có quyền thực thi (chmod +x your_script.sh)
            Process process = Runtime.getRuntime().exec("bash /home/linh2307_hmu/smart-feeds-be/run.sh");

            // Đợi script hoàn thành
            int exitCode = process.waitFor();
            System.out.println("Script đã kết thúc với mã: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Server is running";
    }

}
