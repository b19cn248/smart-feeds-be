package com.olh.feeds.api.service;

import com.olh.feeds.api.controller.gemini.request.GeminiPromptRequest;
import com.olh.feeds.api.controller.gemini.response.GeminiResponseDto;

public interface GeminiService {
    GeminiResponseDto generateContent(GeminiPromptRequest request);
}
