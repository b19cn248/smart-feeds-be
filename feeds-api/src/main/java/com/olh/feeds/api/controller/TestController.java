package com.olh.feeds.api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/test")
@RestController
public class TestController {

    // Test API to check if the server is running
    @RequestMapping("/health")
    public String healthCheck() {
        return "Server is running";
    }

    // Test API to check if the application is up and running
    @RequestMapping("/status")
    public String statusCheck() {
        return "Application is up and running";
    }
}
