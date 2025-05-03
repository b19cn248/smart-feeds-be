package com.olh.feeds.core.websocket.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
  @MessageMapping("/topic/warning")
  @SendTo("/topic/warning")
  public Object message(Object message) {
    return message;
  }
}
