package com.olh.feeds.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/test")
@RestController
public class TestController {

    @GetMapping
    public String test() {
        return "Test API is working!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }
}
