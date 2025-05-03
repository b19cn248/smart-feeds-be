package com.olh.feeds.core.websocket.service;

public interface WebSocketService {
  void sendMessage(String topic, Object message);
}
