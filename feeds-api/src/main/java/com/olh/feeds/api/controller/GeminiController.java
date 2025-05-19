package com.olh.feeds.api.controller;

import com.olh.feeds.api.controller.gemini.request.GeminiPromptRequest;
import com.olh.feeds.api.controller.gemini.response.GeminiResponseDto;
import com.olh.feeds.api.service.GeminiService;
import com.olh.feeds.core.exception.response.ResponseGeneral;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/generate")
    public ResponseGeneral<GeminiResponseDto> generateContent(
            @Validated @RequestBody GeminiPromptRequest request
    ) {
        log.info("REST request to generate content with prompt: {}", request.getPrompt());

        GeminiResponseDto response = geminiService.generateContent(request);

        return ResponseGeneral.of(
                HttpStatus.OK.value(),
                response.isOverLimit() ? "gemini.rate.limit.exceeded" : "gemini.generate.success",
                response
        );
    }
}
