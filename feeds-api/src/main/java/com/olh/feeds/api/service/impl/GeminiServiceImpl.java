package com.olh.feeds.api.service.impl;

import com.olh.feeds.api.controller.gemini.request.ContentDto;
import com.olh.feeds.api.controller.gemini.request.GeminiPromptRequest;
import com.olh.feeds.api.controller.gemini.request.GeminiRequestDto;
import com.olh.feeds.api.controller.gemini.request.PartDto;
import com.olh.feeds.api.controller.gemini.response.GeminiResponseDto;
import com.olh.feeds.api.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:AIzaSyCbAIe1WT9x9_40dv_jkTLgrsTXyKBHJDM}")
    private String defaultApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;

    @Override
    public GeminiResponseDto generateContent(GeminiPromptRequest request) {
        try {
            // Prepare the API key
            String apiKey = request.getApiKey() != null ? request.getApiKey() : defaultApiKey;

            // Create the URL with API key
            String url = geminiApiUrl + "?key=" + apiKey;

            // Prepare the request body
            GeminiRequestDto geminiRequest = createGeminiRequest(request.getPrompt());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create the HTTP entity
            HttpEntity<GeminiRequestDto> entity = new HttpEntity<>(geminiRequest, headers);

            // Make the API call
            ResponseEntity<GeminiResponseDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    GeminiResponseDto.class
            );

            // Get the response
            GeminiResponseDto geminiResponse = response.getBody();

            // Set overLimit flag and original prompt
            if (geminiResponse != null) {
                geminiResponse.setOverLimit(false);
                geminiResponse.setOriginalPrompt(request.getPrompt());
            }

            return geminiResponse;
        } catch (HttpClientErrorException.TooManyRequests ex) {
            log.error("Rate limit exceeded for Gemini API", ex);
            // Create a response with overLimit=true and include the original prompt
            return GeminiResponseDto.builder()
                    .overLimit(true)
                    .originalPrompt(request.getPrompt())
                    .build();
        } catch (Exception ex) {
            log.error("Error calling Gemini API", ex);
            // Create a response with overLimit=true for any other error and include the original prompt
            return GeminiResponseDto.builder()
                    .overLimit(true)
                    .originalPrompt(request.getPrompt())
                    .build();
        }
    }

    private GeminiRequestDto createGeminiRequest(String prompt) {
        PartDto part = PartDto.builder()
                .text(prompt)
                .build();

        ContentDto content = ContentDto.builder()
                .parts(Collections.singletonList(part))
                .build();

        return GeminiRequestDto.builder()
                .contents(Collections.singletonList(content))
                .build();
    }
}
