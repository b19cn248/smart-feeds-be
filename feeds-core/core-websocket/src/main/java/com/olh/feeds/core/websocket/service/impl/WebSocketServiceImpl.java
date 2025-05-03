package com.olh.feeds.core.websocket.service.impl;

import com.olh.feeds.core.websocket.service.WebSocketService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketServiceImpl implements WebSocketService {
  private final SimpMessagingTemplate template;

  public WebSocketServiceImpl(SimpMessagingTemplate template) {
    this.template = template;
  }

  @Override
  @MessageMapping("/{topic}")
  public void sendMessage(String topic, Object message) {
    template.convertAndSend(topic, message);
  }
}
