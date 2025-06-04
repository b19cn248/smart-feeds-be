package com.olh.feeds.service.n8n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class N8nConfig {

    @Value("${n8n.base-url:https://n8n.openlearnhub.io.vn}")
    private String n8nBaseUrl;

    @Value("${n8n.api-key}")
    private String apiKey;

    @Bean
    public RestTemplate n8nRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Thêm interceptor để tự động thêm headers cho mọi request
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("X-N8N-API-KEY", apiKey);
            headers.set("Content-Type", "application/json");
            return execution.execute(request, body);
        };

        restTemplate.setInterceptors(List.of(interceptor));
        return restTemplate;
    }

    @Bean
    public String n8nBaseUrl() {
        return n8nBaseUrl;
    }
}